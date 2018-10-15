package ignite.examples.client;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.cache.CacheException;
import javax.cache.integration.CacheLoaderException;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import ignite.examples.model.JsonData;

public class JsonClient {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) throws ClassNotFoundException {
		Class.forName("org.postgresql.Driver");

		Ignition.setClientMode(true);
		Ignite ignite = Ignition.start("config/config.xml");
		try (IgniteCache<Integer, JsonData> jsonCache = ignite.getOrCreateCache("jsonCache")) {
			/*
			 * Loading all cache
			 */
			
			long begin = System.currentTimeMillis();
			try {
				jsonCache.loadCache(null);
			} catch (CacheLoaderException e) {
				System.err.println("Faliled to load jsonCache");
				throw e;
			}
			long loaded = System.currentTimeMillis();

			System.out.println("[INFO] loaded in " + (loaded - begin) + " mills");

			try {
				System.out.println("[INFO] DATA LOADED, SLEEP(5)");
				TimeUnit.SECONDS.sleep(5);
				System.out.println("[INFO] WAKE UP");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				return;
			}
			
			SqlFieldsQuery query = null;
			QueryCursor<List<?>> cur = null;
			
			begin = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			long full = System.currentTimeMillis();
			try {
				begin = System.currentTimeMillis();
				query = new SqlFieldsQuery("select count(id) from simplejson where data->>'brand' = '\"ACME\"';");
				cur = jsonCache.query(query);
				System.out.println(cur.getAll());
				end = System.currentTimeMillis();
				System.out.println("1st in " + (end-begin) + " mills");

				begin = System.currentTimeMillis();
				query = new SqlFieldsQuery("select count(id) from simplejson where data??'name' and data->>'name' = '\"AC3 Case Red\"';");
				cur = jsonCache.query(query);
				System.out.println(cur.getAll());
				end = System.currentTimeMillis();
				System.out.println("2rd in " + (end-begin) + " mills");

				begin = System.currentTimeMillis();
				query = new SqlFieldsQuery("select count(id) from simplejson where data?&'[type, name, price]';");
				cur = jsonCache.query(query);
				System.out.println(cur.getAll());
				end = System.currentTimeMillis();
				System.out.println("3th in " + (end-begin) + " mills");

				begin = System.currentTimeMillis();
				query = new SqlFieldsQuery("select count(id) from simplejson where data?&'[type, name, price, available]' and data->>'type' = '\"phone\"';");
				cur = jsonCache.query(query);
				System.out.println(cur.getAll());
				end = System.currentTimeMillis();
				System.out.println("4th in " + (end-begin) + " mills");
				
				begin = System.currentTimeMillis();
				query = new SqlFieldsQuery("select count(id) from simplejson where data->'limits'->'voice'->>'n' > 400;");
				cur = jsonCache.query(query);
				System.out.println(cur.getAll());
				end = System.currentTimeMillis();
				System.out.println("5th in " + (end-begin) + " mills");				

				begin = System.currentTimeMillis();
				query = new SqlFieldsQuery("select count(id) from simplejson where data#>>'{limits, voice, n}' > 400;");
				cur = jsonCache.query(query);
				System.out.println(cur.getAll());
				end = System.currentTimeMillis();
				System.out.println("6th in " + (end-begin) + " mills");

				begin = System.currentTimeMillis();
				query = new SqlFieldsQuery("select count(id) from simplejson where data??'color' and data->>'color' = '\"black\"' and data??'price' and data->>'price' = 12.5;");
				cur = jsonCache.query(query);
				System.out.println(cur.getAll());
				end = System.currentTimeMillis();
				System.out.println("7th in " + (end-begin) + " mills");
				
				begin = System.currentTimeMillis();
				query = new SqlFieldsQuery("select count(id) from simplejson where data@>'{\"color\":\"black\", \"price\":12.5}';");
				cur = jsonCache.query(query);
				System.out.println(cur.getAll());
				end = System.currentTimeMillis();
				System.out.println("8th in " + (end-begin) + " mills");

			} catch (Exception e) {
				System.err.println("Failed to execute SqlQuery");
				e.printStackTrace();
				return;
			}

			long quired = System.currentTimeMillis();
			System.out.println("[INFO] full time " + (quired- full) + " mills");
			
		} catch (CacheException e) {
			System.err.println("Failed to get jsonCache");
			e.printStackTrace();
		}

		ignite.close();
	}
}
