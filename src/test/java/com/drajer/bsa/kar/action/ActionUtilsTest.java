package com.drajer.bsa.kar.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.Test;

public class ActionUtilsTest {

  @Test
  public void testIsEncounterChanged() {
    Encounter encounter1 = new Encounter();
    encounter1.setId("helloencounter");
    Encounter encounter2 = new Encounter();
    encounter2.setId("helloencounter");
    assertFalse(ActionUtils.isEncounterChanged(encounter1, encounter2));
    encounter1.getMeta().setLastUpdated(new Date());
    assertTrue(ActionUtils.isEncounterChanged(encounter1, encounter2));
    encounter2.getMeta().setLastUpdated(new Date(System.currentTimeMillis() + 1000L));
    assertTrue(ActionUtils.isEncounterChanged(encounter1, encounter2));
    encounter2.getMeta().setLastUpdated(encounter1.getMeta().getLastUpdated());
    assertFalse(ActionUtils.isEncounterChanged(encounter1, encounter2));

    encounter1.getPeriod().setStart(new Date());
    assertTrue(ActionUtils.isEncounterChanged(encounter1, encounter2));
    encounter2.getPeriod().setStart(new Date(System.currentTimeMillis() + 1000L));
    assertTrue(ActionUtils.isEncounterChanged(encounter1, encounter2));
    encounter2.getPeriod().setStart(encounter1.getPeriod().getStart());
    assertFalse(ActionUtils.isEncounterChanged(encounter1, encounter2));

    encounter1.getPeriod().setEnd(new Date());
    assertTrue(ActionUtils.isEncounterChanged(encounter1, encounter2));
    encounter2.getPeriod().setEnd(new Date(System.currentTimeMillis() + 1000L));
    assertTrue(ActionUtils.isEncounterChanged(encounter1, encounter2));
    encounter2.getPeriod().setEnd(encounter1.getPeriod().getEnd());
    assertFalse(ActionUtils.isEncounterChanged(encounter1, encounter2));
  }
}
