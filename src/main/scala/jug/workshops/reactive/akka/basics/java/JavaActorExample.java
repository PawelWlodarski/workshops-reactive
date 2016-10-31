package jug.workshops.reactive.akka.basics.java;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

/**
 * Created by pawel on 27.03.16.
 */
public class JavaActorExample {

    public static void main(String[] args){
        Props javaActorProps = Props.create(JavaActor.class);
        final ActorSystem system = ActorSystem.create("JavaSystem");

        ActorRef javaActor = system.actorOf(javaActorProps);

        javaActor.tell("hello",ActorRef.noSender());
        javaActor.tell("unknown",ActorRef.noSender());
        javaActor.tell(20,ActorRef.noSender());

        system.terminate();
    }

}

class JavaActor extends UntypedActor{

    @Override
    public void onReceive(Object message) throws Exception {
        if("hello".equals(message)){
            System.out.println("JAVA ACTOR : hello");
        }else if (message instanceof String){
            System.out.println("JAVA ACTOR : received some string : "+message);
        }else{
            System.out.println("JAVA ACTOR :received something else: "+message);
        }
    }
}
