package ahodanenok.di.next.inject;

import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.atinject.tck.Tck;
import org.atinject.tck.auto.*;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.RoundThing;
import org.atinject.tck.auto.accessories.SpareTire;

import javax.inject.Named;
import java.util.Collections;

@Drivers
@Named("spare")
public class InjectTckTest {

    public static boolean skip = true;

    public static Test suite() {
        // workaround for a bug in vintage engine, suite method is called twice
        if (skip) {
            skip = false;
            return new TestSuite();
        }

        World w = new World();
        w.getQueue().add(ClassCharacter.of(Convertible.class));
        w.getQueue().add(ClassCharacter.of(DriversSeat.class)
                .withQualifiers(Collections.singletonList(InjectTckTest.class.getAnnotation(Drivers.class))));
        w.getQueue().add(ClassCharacter.of(SpareTire.class).knownAs("spare"));
        w.getQueue().add(ClassCharacter.of(FuelTank.class));
        w.getQueue().add(ClassCharacter.of(Seat.class));
        w.getQueue().add(ClassCharacter.of(Seatbelt.class));
        w.getQueue().add(ClassCharacter.of(Tire.class));
        w.getQueue().add(ClassCharacter.of(V8Engine.class));
        w.getQueue().add(ClassCharacter.of(Cupholder.class));
        w.getQueue().add(ClassCharacter.of(RoundThing.class));
        w.getQueue().add(ClassCharacter.of(SpareTire.class));
        w.getQueue().flush();

        return Tck.testsFor(w.find(ObjectRequest.of(Car.class)), false, true);
    }
}