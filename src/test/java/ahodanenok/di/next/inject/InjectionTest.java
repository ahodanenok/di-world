package ahodanenok.di.next.inject;

import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.character.ClassCharacter;
import ahodanenok.di.exception.DependencyLookupException;
import ahodanenok.di.next.inject.classes.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InjectionTest {

    @Test
    @DisplayName("should inject members")
    public void injectMembers() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(FoodContainer.class));
        w.getQueue().add(ClassCharacter.of(Bread.class));
        w.getQueue().add(ClassCharacter.of(Butter.class));
        w.getQueue().add(ClassCharacter.of(Capacity.class));
        w.getQueue().add(ClassCharacter.of(Color.class));
        w.getQueue().flush();

        FoodContainer fc = w.find(ObjectRequest.byType(FoodContainer.class));
        assertThat(fc.bread).isExactlyInstanceOf(Bread.class);
        assertThat(fc.butter).isExactlyInstanceOf(Butter.class);
        assertThat(fc.capacity).isExactlyInstanceOf(Capacity.class);
        assertThat(fc.color).isExactlyInstanceOf(Color.class);
    }

    @Test
    @DisplayName("should throw error if no dependencies match")
    public void errorNoDependencies() {
        World w = new World();

        assertThatThrownBy(() -> w.find(ObjectRequest.byType(Drinkable.class)))
                .isExactlyInstanceOf(DependencyLookupException.class)
                .hasMessageStartingWith("No dependencies are found for a request");
    }

    @Test
    @DisplayName("should throw error if multiple dependencies match")
    public void errorNoQualifier() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Coffee.class));
        w.getQueue().add(ClassCharacter.of(Tea.class));
        w.getQueue().flush();

        assertThatThrownBy(() -> w.find(ObjectRequest.byType(Drinkable.class)))
            .isExactlyInstanceOf(DependencyLookupException.class)
            .hasMessageStartingWith("Multiple matching dependencies are found for a request");
    }

    @Test
    @DisplayName("should inject dependency using single qualifier")
    public void singleQualifier() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Coffee.class));
        w.getQueue().add(ClassCharacter.of(Cup.class));
        w.getQueue().add(ClassCharacter.of(Water.class));
        w.getQueue().flush();

        Cup c = w.find(ObjectRequest.byType(Cup.class));
        assertThat(c).isExactlyInstanceOf(Cup.class);
        assertThat(c.drinkable).isExactlyInstanceOf(Coffee.class);
    }

    @Test
    @DisplayName("should inject dependency using multiple qualifier")
    public void multipleQualifiers() {
        World w = new World();
        w.getQueue().add(ClassCharacter.of(Coffee.class));
        w.getQueue().add(ClassCharacter.of(Morning.class));
        w.getQueue().add(ClassCharacter.of(Tea.class));
        w.getQueue().flush();

        Morning m = w.find(ObjectRequest.byType(Morning.class));
        assertThat(m).isExactlyInstanceOf(Morning.class);
        assertThat(m.drink).isExactlyInstanceOf(Coffee.class);
        assertThat(m.injectedDrink).isExactlyInstanceOf(Coffee.class);
    }

//    @Test
//    @DisplayName("should inject all matching in a collection")
//    public void collection() {
//        World w = new World();
//        w.getQueue().add(ClassCharacter.of(Kitchen.class));
//        w.getQueue().add(ClassCharacter.of(Coffee.class));
//        w.getQueue().add(ClassCharacter.of(Water.class));
//        w.getQueue().add(ClassCharacter.of(Tea.class));
//        w.getQueue().flush();
//
//        Kitchen c = w.find(ObjectRequest.byType(Kitchen.class));
//        assertThat(c).isExactlyInstanceOf(Kitchen.class);
//        assertThat(c.drinks).hasSize(3);
//        assertThat(c.drinks.stream().map(Drinkable::getClass)
//                .collect(Collectors.<Class<? extends Drinkable>>toList()))
//                .containsExactlyInAnyOrder(Water.class, Tea.class, Coffee.class);
//    }

//    @Test
//    @DisplayName("should inject all matching in a collection with qualifiers")
//    public void collectionQualifiers() {
//        World w = new World();
//        w.getQueue().add(ClassCharacter.of(Kitchen.class));
//        w.getQueue().add(ClassCharacter.of(Coffee.class));
//        w.getQueue().add(ClassCharacter.of(Water.class));
//        w.getQueue().add(ClassCharacter.of(Tea.class));
//        w.getQueue().flush();
//
//        Kitchen c = w.find(ObjectRequest.byType(Kitchen.class));
//        assertThat(c).isExactlyInstanceOf(Kitchen.class);
//        assertThat(c.hotDrinks).hasSize(2);
//        assertThat(c.hotDrinks.stream().map(Drinkable::getClass)
//                .collect(Collectors.<Class<? extends Drinkable>>toList()))
//                .containsExactlyInAnyOrder(Water.class, Tea.class);
//    }
}
