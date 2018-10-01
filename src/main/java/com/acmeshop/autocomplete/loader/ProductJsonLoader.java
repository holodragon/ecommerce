package com.acmeshop.autocomplete.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.acmeshop.autocomplete.datastore.AbstractAutocomplete.Stats;
import com.acmeshop.autocomplete.datastore.IAutocompleteStore;
import com.acmeshop.pojo.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ProductLoader
 *
 * @author mborges
 *
 */

@Component
public class ProductJsonLoader implements CommandLineRunner {
	// private static final Logger log = LoggerFactory.getLogger(ProductJsonLoader.class);
	private Log log = LogFactory.getLog(ProductJsonLoader.class);

	@Value("${autocomplete.json.load}")
	private Resource loadFile;

	@Autowired
	private IAutocompleteStore store;

	// For init process with -Drun.arguments="anything"
	public void run(String... args) throws Exception {

		if (args.length > 0) {
			log.info("seeding cache...");
			load();
		} else {
			return;
		}

		for (String arg : args) {
			log.info(arg);
		}

	}

	public Stats load() throws Exception {

		log.info("in load");
		try {
			Reader in = new InputStreamReader(loadFile.getInputStream(), "UTF-8");
			Scanner scan = new Scanner(in);
//			JsonReader reader = new JsonReader(in);
//			Gson gson = new GsonBuilder().create();


//			Map<String, Object> jsonMap = mapper.readValue(in, Map.class);
			ObjectMapper mapper = new ObjectMapper();

			List<Product> products = mapper.readValue(in, mapper.getTypeFactory().constructCollectionType(List.class, Product.class));
			// Read file in stream mode
//			reader.beginArray();
			int total = 0;
			for (Product product: products) {
				store.addProduct(product.getSku(), product.getName() == null ? "" : product.getName());
				total++;
				if (total % 100 == 0) {
					log.info(String.format("loaded %d records...", total));
				}
			}
//			while (reader.hasNext()) {
//			while (scan.hasNextLine()) {
//				// Read data into object model
////				Product product = gson.fromJson(reader, Product.class);
//				Product product = mapper.readValue(scan.nextLine(), Product.class);
//				store.addProduct(product.getSku(), product.getName() == null ? "" : product.getName());
//				total++;
//				if (total % 100 == 0) {
//					log.info(String.format("loaded %d records...", total));
//				}
//				// break;
//			}
//			reader.close();
//			scan.close();
			if (in != null) {
				in.close();
			}
			log.info(String.format("LOAD PRODUCTS: Total records: %d ", total));
		} catch (UnsupportedEncodingException ex) {
			log.info("LOAD PRODUCTS: Product source not encoded in UTF-8");
		} catch (IOException ex) {
			log.info("LOAD PRODUCTS: IO Exception");
		}
		return store.stats();
	}

} // class
