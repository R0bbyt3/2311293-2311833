package model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DiceRollTest.class,
    MovementTest.class,
    PropertyPurchaseTest.class,
    ConstructionTest.class,
    RentPaymentTest.class,
    JailTest.class,
    BankruptcyTest.class
})
public class Iteracao1TestSuite { }