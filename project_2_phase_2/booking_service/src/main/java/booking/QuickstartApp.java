package booking;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import booking.actors.BookingActor;
import booking.actors.ShowActor;
import booking.actors.WorkerActor;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.GroupRouter;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.actor.typed.ActorSystem;
import booking.routes.*;

import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletionStage;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;

public class QuickstartApp {
    // #start-http-server
    static void startHttpServer(Route route, ActorSystem<?> system) {
        CompletionStage<ServerBinding> futureBinding =
            Http.get(system).newServerAt("0.0.0.0", 8081).bind(route);

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                InetSocketAddress address = binding.localAddress();
                system.log().info("Server online at http://{}:{}/",
                    address.getHostString(),
                    address.getPort());
            } else {
                system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                system.terminate();
            }
        });
    }
    // #start-http-server

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        System.out.println("port is - "+port);
        startup(port);
    }

    private static ActorSystem<Void> startup(int port) {
        // Override the configuration of the port
        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);

        Config config = ConfigFactory.parseMap(overrides)
            .withFallback(ConfigFactory.load());

        // Create an Akka system
        ActorSystem<Void> system = ActorSystem.create(rootBehavior(port), "ClusterSystem", config);

        return system;
    }

    private static Behavior<Void> rootBehavior(int port){
        return Behaviors.setup(context -> {

            //create sharding
            final ClusterSharding sharding = ClusterSharding.get(context.getSystem());
            sharding.init(
                Entity.of(ShowActor.TypeKey,entityContext -> ShowActor.create(entityContext.getEntityId())
            ));

            List<EntityRef<ShowActor.Command>> shows = new ArrayList<>();

            //create the list of showActor entity
            for(int i=1;i<=20;i++){
                shows.add(sharding.entityRefFor(ShowActor.TypeKey, Integer.toString(i)));
            }

            List<ActorRef<WorkerActor.Command>> workers = new ArrayList<>();

            //create the list of worker actor
            for(int i=0;i<40;i++){
                workers.add(context.spawn(WorkerActor.create(shows), "workerActor"+Integer.toString(i)));
                context.getSystem().receptionist().tell(Receptionist.register(WorkerActor.serviceKey, workers.get(i)));
            }

            //initialize group routes
            GroupRouter<WorkerActor.Command> group= Routers.group(WorkerActor.serviceKey);
            ActorRef<WorkerActor.Command> router = context.spawn(group, "worker-group");
           
            //spawn the booking actor
            if(port==8083){
                ActorRef<BookingActor.Command> bookingActor = context.spawn(BookingActor.create(router), "BookingActor");
                //creating the instances of route classes
                TheatreRoutes theatreRoutes = new TheatreRoutes(context.getSystem(), bookingActor);
                ShowRoutes showRoutes = new ShowRoutes(context.getSystem(), bookingActor);
                BookingRoutes bookingRoutes = new BookingRoutes(context.getSystem(), bookingActor);
                AllRoutes allRoutes = new AllRoutes(theatreRoutes, showRoutes, bookingRoutes);
        
                startHttpServer(allRoutes.allRoutes(), context.getSystem());
            }

            return Behaviors.empty();
        });
    }

}
