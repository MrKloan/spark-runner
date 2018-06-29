package spark.runner.config.consumers;

import spark.Spark;
import spark.runner.config.ApplicationConfig;

import java.util.Optional;
import java.util.function.Consumer;

public final class SslConfigConsumer implements Consumer<ApplicationConfig> {
	
	private static final String KEYSTORE = "spark.keystore";
	private static final String KEYSTORE_PASSWORD = "spark.keystore.password";
	private static final String TRUSTSTORE = "spark.truststore";
	private static final String TRUSTSTORE_PASSWORD = "spark.truststore.password";
	
	@Override
	public void accept(final ApplicationConfig applicationConfig) {
		final Optional<String> keystore = applicationConfig.getString(KEYSTORE);
		final Optional<String> keystorePassword = applicationConfig.getString(KEYSTORE_PASSWORD);
		
		if(!keystore.isPresent() || !keystorePassword.isPresent())
			return;
		
		final Optional<String> truststore = applicationConfig.getString(TRUSTSTORE);
		final Optional<String> truststorePassword = applicationConfig.getString(TRUSTSTORE_PASSWORD);
		
		if(truststore.isPresent() && truststorePassword.isPresent())
			Spark.secure(keystore.get(), keystorePassword.get(), truststore.get(), truststorePassword.get());
		else
			Spark.secure(keystore.get(), keystorePassword.get(), null, null);
	}
}
