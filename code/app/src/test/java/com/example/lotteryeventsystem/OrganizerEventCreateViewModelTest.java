package com.example.lotteryeventsystem;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class OrganizerEventCreateViewModelTest {

    // TODO: this test case is having issues because of the usage of private MutableLiveData<String> in the class.

    private OrganizerEventCreateViewModel viewModel;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter timeFormatter;

    @Before
    public void setUp() {
        viewModel = new OrganizerEventCreateViewModel();
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    }

    @Test
    public void testIsEventValid_AllFieldsFilled_ReturnsTrue() {
        viewModel.setName("TestEvent");
        viewModel.setDescription("Description for Test Event");
        viewModel.setDate("2026-01-01");
        viewModel.setEventStart("12:00 PM");
        viewModel.setEventEnd("04:00 PM");

        assertTrue(viewModel.isEventValid());
    }

    @Test
    public void testIsEventValid_MissingName_ReturnsFalse() {
        viewModel.setDescription("Description for Test Event");
        viewModel.setDate("2026-01-01");
        viewModel.setEventStart("12:00 PM");
        viewModel.setEventEnd("04:00 PM");

        assertFalse(viewModel.isEventValid());
    }

    @Test
    public void testIsEventValid_MissingDescription_ReturnsFalse() {
        viewModel.setName("TestEvent");
        viewModel.setDate("2026-01-01");
        viewModel.setEventStart("12:00 PM");
        viewModel.setEventEnd("04:00 PM");

        assertFalse(viewModel.isEventValid());
    }

    @Test
    public void testIsEventValid_MissingDate_ReturnsFalse() {
        viewModel.setName("TestEvent");
        viewModel.setDescription("Description for Test Event");
        viewModel.setEventStart("12:00 PM");
        viewModel.setEventEnd("04:00 PM");

        assertFalse(viewModel.isEventValid());
    }

    @Test
    public void testIsEventValid_MissingEventStart_ReturnFalse() {
        viewModel.setName("TestEvent");
        viewModel.setDescription("Description for Test Event");
        viewModel.setDate("2026-01-01");
        viewModel.setEventEnd("04:00 PM");

        assertFalse(viewModel.isEventValid());
    }

    @Test
    public void testIsEventValid_MissingEventEnd_ReturnsFalse() {
        viewModel.setName("TestEvent");
        viewModel.setDescription("Description for Test Event");
        viewModel.setDate("2026-01-01");
        viewModel.setEventStart("12:00 PM");

        assertFalse(viewModel.isEventValid());
    }

    @Test
    public void testEventDateValid_FutureDate_ReturnsTrue() {
        assertTrue(viewModel.eventDateValid("2030-01-01", dateFormatter));
    }

    @Test
    public void testEventDateValid_PastDate_ReturnsFalse() {
        assertFalse(viewModel.eventDateValid("2025-01-01", dateFormatter));
    }

    @Test
    public void testRegistrationPeriodValid_AllTrue_ReturnsTrue() {
        String eventDate = "2030-01-01";
        String regStart = "2029-06-01";
        String regEnd = "2029-07-01";

        assertTrue(viewModel.registrationPeriodValid(eventDate, regStart, regEnd, dateFormatter));
    }

    @Test
    public void testRegistrationPeriodValid_regStartAfterRegEnd_ReturnsFalse() {
        String eventDate = "2030-01-01";
        String regStart = "2029-07-01";
        String regEnd = "2029-06-01";

        assertFalse(viewModel.registrationPeriodValid(eventDate, regStart, regEnd, dateFormatter));
    }

    @Test
    public void testRegistrationPeriodValid_curDateAfterRegEnd_ReturnsFalse() {
        String eventDate = "2025-01-01";
        String regStart = "2025-06-01";
        String regEnd = "2025-07-01";

        assertFalse(viewModel.registrationPeriodValid(eventDate, regStart, regEnd, dateFormatter));
    }

    @Test
    public void testRegistrationPeriodValid_regEndAfterEventDate_ReturnsFalse() {
        String eventDate = "2030-01-01";
        String regStart = "2029-06-01";
        String regEnd = "2030-02-01";

        assertFalse(viewModel.registrationPeriodValid(eventDate, regStart, regEnd, dateFormatter));
    }

    @Test
    public void testEventTimeValid_startTimeBeforeEndTime_ReturnsTrue() {
        String startTime = "12:00 PM";
        String endTime = "03:00 PM";

        assertTrue(viewModel.eventTimeValid(startTime, endTime, timeFormatter));
    }

    @Test
    public void testEventTimeValid_startTimeAfterEndTime_ReturnsFalse() {
        String startTime = "03:00 PM";
        String endTime = "12:00 PM";

        assertFalse(viewModel.eventTimeValid(startTime, endTime, timeFormatter));
    }

    @Test
    public void testClearForm_ResetAllValues() {
        viewModel.setName("TestEvent");
        viewModel.setDescription("Description for Test Event");
        viewModel.setDate("2026-01-01");
        viewModel.setEventStart("12:00 PM");
        viewModel.setEventEnd("04:00 PM");
        viewModel.clearForm();

        assertNull(viewModel.getNameValue());
    }
}
