package ignite.examples.client;

import java.util.List;

import javax.cache.CacheException;
import javax.cache.integration.CacheLoaderException;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.transactions.TransactionException;

import ignite.examples.model.Person;

public class Client {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws ClassNotFoundException {
		Class.forName("org.postgresql.Driver");

		Ignition.setClientMode(true);
		Ignite ignite = Ignition.start("config/postgres-person.xml");
		try (IgniteCache<Long, Person> personCache = ignite.getOrCreateCache("personCache")) {
			/*
			 * Loading all cache
			 */
			try {
				personCache.loadCache(null);
			} catch (CacheLoaderException e) {
				System.err.println("Faliled to load personCache");
				throw e;
			}
//			
			/*
			 * Working with the cache using built-in functions
			 */
			try {
				Person person = personCache.get(new Long(1));
				System.out.println();
				System.out.printf("Loaded object %s\n", person.toString());
				System.out.println();
			} catch (TransactionException e) {
				System.err.println("Failed to get object with key = 1");
				e.printStackTrace();
			}
			
			/*
			 * Working with cache using queries
			 */
			try {
				SqlQuery query = new SqlQuery(Person.class, "select * from person");
				QueryCursor<List<?>> cur = personCache.query(query);
				
				System.out.println("Queried values:");
				System.out.println(cur.getAll().toString());
				System.out.println();
			} catch (Exception e) {
				System.err.println("Failed to execute SqlQuery");
				e.printStackTrace();
			}
			

			try {
				SqlQuery query = new SqlQuery(Person.class, "select * from person where id%2 > 0");
				query.setArgs(new Long(3));
				QueryCursor<List<?>> cur = personCache.query(query);
				System.out.println("Queried values:");
				System.out.println(cur.getAll().toString());
				System.out.println();
			} catch (Exception e) {
				System.err.println("Failed to execute SqlQuery");
				e.printStackTrace();
			}

			/*
			 * Excepted error because cached only holistic Person objects 
			 */
			try {
				SqlQuery query = new SqlQuery(Person.class, "select first_name from person");
				QueryCursor<List<?>> cur = personCache.query(query);
				System.out.println("Queried values:");
				System.out.println(cur.getAll().toString());
			} catch (Exception e) {
				System.out.println();
				System.out.println("Only \"select * from table where ...\" provided");
				System.out.println();
			}		
		} catch (CacheException e) {
			System.err.println("Failed to get personCache");
			e.printStackTrace();
		}

		ignite.close();
	}

}
