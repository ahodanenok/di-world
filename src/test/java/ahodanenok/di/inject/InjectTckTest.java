package ahodanenok.di.inject;

import ahodanenok.di.DefaultWorld;
import ahodanenok.di.ObjectRequest;
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

        DefaultWorld w = new DefaultWorld();
        w.getQueue().add(ClassCharacter.of(Convertible.class));
        w.getQueue().add(ClassCharacter.of(DriversSeat.class)
                .qualifiedAs(Collections.singletonList(InjectTckTest.class.getAnnotation(Drivers.class))));
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

        StaticInjector staticInjector = new StaticInjector(w);
        staticInjector.addClass(Convertible.class);
        staticInjector.addClass(Tire.class);
        staticInjector.addClass(SpareTire.class);
        staticInjector.addClass(RoundThing.class);
        staticInjector.inject();

        Car car = w.find(ObjectRequest.of(Car.class));
        if (car == null) {
            throw new IllegalStateException("Car is null!");
        }

        return Tck.testsFor(car, true, true);
    }
}