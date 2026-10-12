package kz.nurlybek.coworking_booking_api.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Сквозной сценарий через HTTP: JWT, роли, бронирование, оплата, документы. */
@SpringBootTest
@AutoConfigureMockMvc
class BookingApiIntegrationTest {

    private static final AtomicInteger USER_SEQ = new AtomicInteger();

    @Autowired private MockMvc mvc;

    private String adminToken;
    private long roomId;

    @BeforeEach
    void setUp() throws Exception {
        // Администратор создаётся AdminBootstrap из тестового application.yaml.
        adminToken = login("admin@test.local", "admin-password");
        long locationId = createdId(post("/api/locations"), adminToken, """
                {"name":"Center","address":"Abay 1","city":"Almaty"}""");
        roomId = createdId(post("/api/rooms"), adminToken, """
                {"name":"Blue","capacity":6,"hourlyBasePrice":1000,"locationId":%d}""".formatted(locationId));
    }

    @Test
    void fullBookingFlow() throws Exception {
        String client = registerClient();
        String day = LocalDate.now().plusDays(1).toString();

        long bookingId = createdId(post("/api/bookings"), client, """
                {"roomId":%d,"startTime":"%sT10:00:00","endTime":"%sT12:00:00"}""".formatted(roomId, day, day));

        mvc.perform(post("/api/bookings/{id}/payment", bookingId).header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"method\":\"CARD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.amount").value(2000.0));

        mvc.perform(get("/api/bookings/{id}", bookingId).header(HttpHeaders.AUTHORIZATION, bearer(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"));

        mvc.perform(get("/api/bookings/{id}/confirmation", bookingId).header(HttpHeaders.AUTHORIZATION, bearer(client)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"booking-%d-confirmation.docx\"".formatted(bookingId)));

        // Тот же слот второй раз забронировать нельзя.
        mvc.perform(withJson(post("/api/bookings"), registerClient(), """
                        {"roomId":%d,"startTime":"%sT11:00:00","endTime":"%sT13:00:00"}""".formatted(roomId, day, day)))
                .andExpect(status().isConflict());
    }

    @Test
    void clientSeesOnlyOwnBookings() throws Exception {
        String owner = registerClient();
        String day = LocalDate.now().plusDays(2).toString();
        createdId(post("/api/bookings"), owner, """
                {"roomId":%d,"startTime":"%sT14:00:00","endTime":"%sT15:00:00"}""".formatted(roomId, day, day));

        mvc.perform(get("/api/bookings").header(HttpHeaders.AUTHORIZATION, bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/bookings").header(HttpHeaders.AUTHORIZATION, bearer(registerClient())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void requestWithoutTokenIsUnauthorized() throws Exception {
        mvc.perform(get("/api/bookings")).andExpect(status().isUnauthorized());
    }

    @Test
    void catalogIsPublicForReading() throws Exception {
        mvc.perform(get("/api/rooms/{id}", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Blue"));
    }

    @Test
    void clientCannotManageRooms() throws Exception {
        mvc.perform(withJson(post("/api/rooms"), registerClient(), """
                        {"name":"Red","capacity":2,"hourlyBasePrice":500,"locationId":1}"""))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRequestReturnsFieldErrors() throws Exception {
        mvc.perform(withJson(post("/api/auth/register"), null, """
                        {"email":"not-an-email","password":"short","fullName":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    void unknownSortPropertyIsBadRequest() throws Exception {
        mvc.perform(get("/api/rooms").param("sort", "noSuchField"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void managerDownloadsExcelReport() throws Exception {
        String managerToken = registerClient();
        long managerId = JsonPath.<Integer>read(mvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(managerToken)))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mvc.perform(withJson(patch("/api/users/{id}", managerId), adminToken, "{\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk());

        String today = LocalDate.now().toString();
        mvc.perform(get("/api/reports/occupancy").param("from", today).param("to", today)
                        .header(HttpHeaders.AUTHORIZATION, bearer(managerToken)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    private String registerClient() throws Exception {
        String email = "client" + USER_SEQ.incrementAndGet() + "@test.local";
        String body = mvc.perform(withJson(post("/api/auth/register"), null, """
                        {"email":"%s","password":"password1","fullName":"Client"}""".formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(withJson(post("/api/auth/login"), null, """
                        {"email":"%s","password":"%s"}""".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private long createdId(MockHttpServletRequestBuilder request, String token, String json) throws Exception {
        String body = mvc.perform(withJson(request, token, json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.<Integer>read(body, "$.id");
    }

    private static MockHttpServletRequestBuilder withJson(MockHttpServletRequestBuilder request,
                                                          String token, String json) {
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer(token));
        }
        return request.contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
