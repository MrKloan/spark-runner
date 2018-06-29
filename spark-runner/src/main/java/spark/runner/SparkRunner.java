package spark.runner;

import io.fries.reflection.Reflection;
import io.fries.reflection.filters.AnnotationFilter;
import io.fries.reflection.filters.PackageFilter;
import io.fries.reflection.scanners.ClassPathScanner;
import io.fries.reflection.scanners.Scanner;
import spark.ResponseTransformer;
import spark.Spark;
import spark.runner.annotations.*;
import spark.runner.config.ApplicationConfig;
import spark.runner.config.ResourceBundleConfig;
import spark.runner.config.SparkConfig;
import spark.runner.exceptions.SparkRunnerException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static java.lang.Thread.currentThread;

public final class SparkRunner {
	
	private final Class applicationClass;
	
	/**
	 * Create a new SparkRunner object which will take care of all the application initialisation and configuration.
	 *
	 * @param applicationClass The base class of this Spark application.
	 */
	private SparkRunner(final Class applicationClass) throws SparkRunnerException {
		this.applicationClass = applicationClass;
		
		final Object application = initApplication();
		
		// Configure the application
		initSparkConfiguration().load();
		
		// Gather all the application classes using reflection
		final Reflection reflection = getReflectionEngine();
		
		final Set<Class<?>> components = scanApplicationComponents(reflection);
		final Set<Class<?>> webSockets = scanApplicationWebSockets(reflection);
		
		if(components.isEmpty() && webSockets.isEmpty())
			throw new IllegalStateException("No Spark component could be found.");
		
		storeComponents(components);
		storeComponents(webSockets);
		
		// Process injections in the main Application class
		injectFields(application, applicationClass);
		injectExceptions(application, applicationClass);
		
		// WebSockets need to be initialized first
		processWebSocketsInjection(webSockets);
		
		// Then we can register other components
		processComponentsInjection(components);
		
		// Wait for the server initialization before proceeding
		Spark.awaitInitialization();
	}
	
	/**
	 * @param applicationClass The application class, used as an entry point for package scanning
	 *                         and runtime annotations instantiations.
	 */
	public static void startApplication(final Class<?> applicationClass) throws SparkRunnerException {
		if(!applicationClass.isAnnotationPresent(SparkApplication.class))
			throw new IllegalStateException("Application class must be annotated using @SparkApplication.");
		
		new SparkRunner(applicationClass);
	}
	
	/**
	 * @return A new instance of the main application class.
	 *
	 * @throws SparkRunnerException When the main application class cannot be instantiated.
	 */
	private Object initApplication() throws SparkRunnerException {
		final Object app = createClassInstance(applicationClass);
		return SparkComponentStore.put(app);
	}
	
	private SparkConfig initSparkConfiguration() {
		final SparkApplication sparkApplication = (SparkApplication) applicationClass.getAnnotation(SparkApplication.class);
		final ApplicationConfig applicationConfig = ResourceBundleConfig.of(sparkApplication.resourceBundle());
		
		// SparkComponentStore.put(applicationConfig);
		return SparkConfig.of(applicationConfig);
	}
	
	/**
	 * @return The configured reflection engine used to gather classes using their annotations.
	 */
	private Reflection getReflectionEngine() {
		final String rootPackage = applicationClass.getPackage().getName();
		
		final ClassLoader classLoader = currentThread().getContextClassLoader();
		final Scanner scanner = ClassPathScanner
			.of(classLoader)
			.filter(PackageFilter.withSubpackages(rootPackage))
			.filter(AnnotationFilter.any(Component.class, Controller.class, WebSocket.class));
		
		return Reflection.of(scanner);
	}
	
	/**
	 * @param reflection The reflection engine.
	 *
	 * @return A set of classes annotated using either the {@code SparkComponent} or {@code SparkController} annotations.
	 */
	private Set<Class<?>> scanApplicationComponents(final Reflection reflection) {
		final Set<Class<?>> components = reflection.getAnnotatedTypes(Component.class);
		final Set<Class<?>> controllers = reflection.getAnnotatedTypes(Controller.class);
		components.addAll(controllers);
		
		return components;
	}
	
	/**
	 * @param reflection The reflection engine.
	 *
	 * @return A set of classes annotated using the {@code SparkWebSocket} annotation.
	 */
	private Set<Class<?>> scanApplicationWebSockets(final Reflection reflection) {
		return reflection.getAnnotatedTypes(WebSocket.class);
	}
	
	/**
	 * For each SparkComponent class, a new instance is created and stored in order to be injected.
	 *
	 * @param sparkComponents The set containing all component classes.
	 */
	private void storeComponents(final Set<Class<?>> sparkComponents) throws SparkRunnerException {
		if(sparkComponents == null || sparkComponents.isEmpty())
			return;
		
		for(Class<?> componentClass : sparkComponents) {
			final Object component = createClassInstance(componentClass);
			SparkComponentStore.put(component);
		}
	}
	
	/**
	 * Once the application's components have been instantiated and stored, we can proceed with the injections.
	 * For each component classes, we search its fields for an injection annotation and set its value with the
	 * right component.
	 * If the component is a SparkWebSocket, then its instance will be bound to the specified path.
	 *
	 * @param sparkWebSockets The set containing all web socket classes.
	 */
	private void processWebSocketsInjection(final Set<Class<?>> sparkWebSockets) throws SparkRunnerException {
		if(sparkWebSockets == null || sparkWebSockets.isEmpty())
			return;
		
		for(Class<?> webSocketClass : sparkWebSockets) {
			final Object webSocket = SparkComponentStore.get(webSocketClass);
			
			injectFields(webSocket, webSocketClass);
			
			final WebSocket sparkWebSocket;
			if((sparkWebSocket = webSocketClass.getAnnotation(WebSocket.class)) != null) {
				Spark.webSocket(sparkWebSocket.path(), webSocket);
			}
		}
		
		Spark.init();
	}
	
	/**
	 * Once the application's components have been instantiated and stored, we can proceed with the injections.
	 * For each component classes, we search its fields for an injection annotation and set its value with the
	 * right component.
	 * If the component is a SparkController, then its methods will also be scanned in order to
	 * inject its routes into Spark Routes' lambda expressions.
	 *
	 * @param sparkComponents The set containing all component classes.
	 */
	private void processComponentsInjection(final Set<Class<?>> sparkComponents) throws SparkRunnerException {
		if(sparkComponents == null || sparkComponents.isEmpty())
			return;
		
		for(Class<?> componentClass : sparkComponents) {
			final Object component = SparkComponentStore.get(componentClass);
			
			injectFields(component, componentClass);
			injectExceptions(component, componentClass);
			
			Controller sparkController;
			if((sparkController = componentClass.getAnnotation(Controller.class)) != null) {
				injectFilters(component, sparkController, componentClass);
				injectRoutes(component, sparkController, componentClass);
			}
		}
	}
	
	/**
	 * Search the component's fields for an injection annotation and set its value with the right component.
	 *
	 * @param component      The stored instance of a given component.
	 * @param componentClass The component's class used for fields reflection.
	 */
	private void injectFields(final Object component, final Class<?> componentClass) throws SparkRunnerException {
		final Field[] fields = componentClass.getDeclaredFields();
		
		for(final Field field : fields) {
			if(!field.isAnnotationPresent(Inject.class))
				continue;
			
			final boolean accessible = field.isAccessible();
			
			try {
				final Object value = SparkComponentStore.get(field.getType());
				
				field.setAccessible(true);
				field.set(component, value);
			}
			catch(final IllegalAccessException e) {
				throw new SparkRunnerException("Unable to inject value in field " + field.getName() + " of component " + componentClass.getName(), e);
			}
			finally {
				field.setAccessible(accessible);
			}
		}
	}
	
	/**
	 * Scan the component's methods in order to inject its exception handlers into the Spark engine.
	 *
	 * @param component      The stored instance of a given component.
	 * @param componentClass The component's class used for methods reflection.
	 */
	private void injectExceptions(final Object component, final Class<?> componentClass) {
		final Method[] methods = componentClass.getDeclaredMethods();
		
		for(Method method : methods) {
			final ExceptionHandler sparkException;
			
			if((sparkException = method.getAnnotation(ExceptionHandler.class)) == null)
				continue;
			
			method.setAccessible(true);
			Spark.exception(sparkException.value(), (ex, req, res) -> {
				try {
					method.invoke(component, ex, req, res);
				}
				catch(final IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			});
		}
	}
	
	/**
	 * Scan the component's methods in order to inject its filters into the Spark engine.
	 *
	 * @param component      The stored instance of a given component.
	 * @param componentClass The component's class used for methods reflection.
	 */
	private void injectFilters(final Object component, final Controller sparkController, final Class<?> componentClass) throws SparkRunnerException {
		final Method[] methods = componentClass.getDeclaredMethods();
		
		try {
			final String controllerPath = formatPath(sparkController.path());
			
			for(final Method method : methods) {
				final Filter sparkFilter;
				
				if((sparkFilter = method.getAnnotation(Filter.class)) == null)
					continue;
				
				String filterPath = controllerPath + formatPath(sparkFilter.path());
				if(filterPath.isEmpty())
					filterPath = "*";
				
				method.setAccessible(true);
				
				switch(sparkFilter.type()) {
					case BEFORE:
						Spark.before(filterPath, (req, res) -> method.invoke(component, req, res));
						break;
					case AFTER:
						Spark.after(filterPath, (req, res) -> method.invoke(component, req, res));
						break;
				}
			}
		}
		catch(final URISyntaxException e) {
			throw new SparkRunnerException("Spark component path is invalid.", e);
		}
	}
	
	/**
	 * Scan the component's methods in order to inject its routes into Spark Routes' lambda expressions.
	 *
	 * @param component      The stored instance of a given component.
	 * @param componentClass The component's class used for methods reflection.
	 */
	private void injectRoutes(final Object component, final Controller sparkController, final Class<?> componentClass) throws SparkRunnerException {
		final Method[] methods = componentClass.getDeclaredMethods();
		
		try {
			final String controllerPath = formatPath(sparkController.path());
			
			for(final Method method : methods) {
				final Route sparkRoute;
				
				if((sparkRoute = method.getAnnotation(Route.class)) == null)
					continue;
				
				final String routePath = controllerPath + formatPath(sparkRoute.path());
				final spark.Route routeLambda = createSparkRoute(component, sparkRoute, method);
				final ResponseTransformer routeTransformer = (ResponseTransformer) createClassInstance(sparkRoute.transformer());
				
				method.setAccessible(true);
				
				switch(sparkRoute.method()) {
					case POST:
						Spark.post(routePath, routeLambda, routeTransformer);
						break;
					case PUT:
						Spark.put(routePath, routeLambda, routeTransformer);
						break;
					case PATCH:
						Spark.patch(routePath, routeLambda, routeTransformer);
						break;
					case DELETE:
						Spark.delete(routePath, routeLambda, routeTransformer);
						break;
					case HEAD:
						Spark.head(routePath, routeLambda, routeTransformer);
						break;
					case TRACE:
						Spark.trace(routePath, routeLambda, routeTransformer);
						break;
					case CONNECT:
						Spark.connect(routePath, routeLambda, routeTransformer);
						break;
					case OPTIONS:
						Spark.options(routePath, routeLambda, routeTransformer);
						break;
					default:
						Spark.get(routePath, routeLambda, routeTransformer);
				}
			}
		}
		catch(final URISyntaxException e) {
			throw new SparkRunnerException("Spark component path is invalid.", e);
		}
	}
	
	/**
	 * @param component  The SparkComponent object from which the method will be invoked.
	 * @param sparkRoute The component's SparkRoute annotation containing the route's metadata.
	 * @param method     The class' method to be invoked when the given route is reached.
	 *
	 * @return A Spark Route object to be bound to a certain endpoint.
	 */
	private spark.Route createSparkRoute(final Object component, final Route sparkRoute, final Method method) {
		return (req, res) -> {
			if(!sparkRoute.accept().isEmpty())
				res.header("Accept", sparkRoute.accept());
			res.type(sparkRoute.contentType());
			
			return method.invoke(component, req, res);
		};
	}
	
	/**
	 * @param c The class we wish to instantiate.
	 *
	 * @return An instantiated object using either an empty class constructor or the default Object constructor.
	 *
	 * @throws SparkRunnerException If none of the two instantiation methods were successful.
	 */
	private Object createClassInstance(final Class<?> c) throws SparkRunnerException {
		try {
			return c.getConstructor().newInstance();
		}
		catch(final InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			/* Do nothing */
		}
		
		try {
			return c.newInstance();
		}
		catch(final InstantiationException | IllegalAccessException e) {
			throw new SparkRunnerException(e.getMessage(), e);
		}
	}
	
	private String formatPath(final String path) throws URISyntaxException {
		final URI uri = new URI(path);
		
		String wellFormedUri = uri.toString().replaceAll("/{2,}", "/");
		if(wellFormedUri.endsWith("/"))
			wellFormedUri = wellFormedUri.substring(0, wellFormedUri.length() - 1);
		
		return wellFormedUri;
	}
}
