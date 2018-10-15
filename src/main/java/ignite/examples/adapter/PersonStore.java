package ignite.examples.adapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.cache.Cache.Entry;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import javax.sql.DataSource;

import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.SpringResource;

import ignite.examples.model.Person;

public class PersonStore extends CacheStoreAdapter<Long, Person> {

	@SpringResource(resourceName="dataSource")
	private DataSource dataSource;
	

	public Person load(Long key) {
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement("select * from person where id = ?")) {
				stmt.setLong(1, key);
				ResultSet rs = stmt.executeQuery();
				return rs.next() ? new Person(rs.getLong(1), rs.getString(2), rs.getString(3)) : null;
			} catch (SQLException e) {
	            throw new CacheLoaderException("Failed to load object [key=" + key + ']', e);
	        }
		} catch (SQLException e) {
            throw new CacheLoaderException("Failed to get connection when load object [key=" + key + ']', e);
        }
	}

	
	public void write(Entry<? extends Long, ? extends Person> entry) {
		Long key = entry.getKey();
		Person val = entry.getValue();
		try (Connection conn = dataSource.getConnection()) {
			int updated;
			try (PreparedStatement stmt = conn.prepareStatement(
					"update person set first_name = ?, last_name = ? where id = ?")) {
				stmt.setString(1, val.getFirstName());
				stmt.setString(2, val.getLastName());
				stmt.setLong(3, val.getId());
				
				updated = stmt.executeUpdate();
			} catch (SQLException e) {
	            throw new CacheLoaderException("Failed to update object [key=" + key + ']', e);
	        }
			if (updated == 0) {
				try (PreparedStatement stmt = conn.prepareStatement(
	                    "insert into person (id, first_name, last_name) values (?::bigint, ?, ?)")) {
					stmt.setLong(1, val.getId());
					stmt.setString(2, val.getFirstName());
					stmt.setString(3, val.getLastName());

					stmt.executeUpdate();
				} catch (SQLException e) {
		            throw new CacheLoaderException("Failed to insert object [key=" + key + ']', e);
		        }
			}
		} catch (SQLException e) {
            throw new CacheLoaderException("Failed to get connection when write object [key=" + key + ']', e);
        }
	}

	@Override
	public void delete(Object key) throws CacheWriterException {
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement("delete from PERSON where id=?")) {
				stmt.setLong(1, (Long) key);
				stmt.executeUpdate();
			} catch (SQLException e) {
	            throw new CacheWriterException("Failed to delete object [key=" + key + ']', e);
	        }
		} catch (SQLException e) {
            throw new CacheWriterException("Failed to get connection when delete object [key=" + key + ']', e);
        }
	}

	public void loadCache(IgniteBiInClosure<Long, Person> clo, Object... args) throws CacheLoaderException {
		try (Connection conn = dataSource.getConnection()) {
			try (PreparedStatement stmt = conn.prepareStatement("select * from Person")) {
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					Person person = new Person (rs.getLong(1), rs.getString(2), rs.getString(3));
					clo.apply(person.getId(), person);
				}
			} catch (SQLException e) {
				throw new CacheLoaderException("Failed to load values from cache store.", e);
			}
		} catch (SQLException e1) {
			throw new CacheLoaderException("Failed to get connection when load values.", e1);
		}
	}

}
