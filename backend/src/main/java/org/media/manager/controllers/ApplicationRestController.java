package org.media.manager.controllers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.media.manager.dao.AppUserDAO;
import org.media.manager.dao.TicketDao;
import org.media.manager.dao.TravelConnectionDAO;
import org.media.manager.dto.AppUserDTO;
import org.media.manager.entity.AppUser;
import org.media.manager.entity.Connection;
import org.media.manager.entity.Ticket;
import org.media.manager.enums.TicketType;
import org.media.manager.mapper.AppUserMapper;
import org.media.manager.mapper.TicketMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.sql.Time;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class ApplicationRestController {

    private final AppUserMapper appUserMapper;

    private final TicketMapper ticketMapper;

    private final TravelConnectionDAO travelConnectionDAO;

    private final AppUserDAO appUserDAO;

    private final TicketDao ticketDao;

    private Gson gson;

    @Autowired
    public ApplicationRestController(AppUserMapper appUserMapper, TicketMapper ticketMapper, TravelConnectionDAO travelConnectionDAO, AppUserDAO appUserDAO, TicketDao ticketDao) {
        this.appUserMapper = appUserMapper;
        this.ticketMapper = ticketMapper;
        this.travelConnectionDAO = travelConnectionDAO;
        this.appUserDAO = appUserDAO;
        this.ticketDao = ticketDao;
    }

    @PostConstruct
    public void initialize (){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    @GetMapping("/connection/{from}/to/{to}/sinceHour/{time}")
    public String getConnectionsByStationAndTime(@PathVariable String from, @PathVariable String to, @PathVariable Time time){
        Set<Connection> connections = travelConnectionDAO.findConnectionsByTimeGreaterThanEqualAndFromStationAndToStation(time, from, to);
        return gson.toJson(connections) ;
    }

    @GetMapping("/assignTicket/{connectionId}/user/{userId}/ticket_type/{ticketType}/travelDate/{travelDate}")
    public void assignTicketToUser(@PathVariable long connectionId, @PathVariable long userId, @PathVariable TicketType ticketType, @PathVariable Date travelDate){
        Ticket ticket = new Ticket();
        ticket.setTicketType(ticketType);
        Connection connection = travelConnectionDAO.findById(connectionId).orElseThrow(() -> new IllegalArgumentException("Travel connection does not exist"));
        AppUser appUser = appUserDAO.findById(userId).orElseThrow(() -> new IllegalArgumentException("User does not exist"));
        ticket.setConnection(connection);
        ticket.setAppUser(appUser);
        ticket.setTravelDate(travelDate);
        ticketDao.save(ticket);

    }

    @GetMapping("/tickets/{userId}")
    public String getTicketsOfUser(@PathVariable long userId){
        Set<Ticket> tickets = ticketDao.findByAppUser_Id(userId);
        return gson.toJson(tickets.stream().map(ticketMapper::mapTicket).collect(Collectors.toSet()));

    }

    @GetMapping("/checkUser/{username}")
    public boolean userExists(@PathVariable String username){
        return appUserDAO.existsByUsername(username);
    }

    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    public ResponseEntity<String> handlePreconditionFailed(DataIntegrityViolationException exception) {
        exception.printStackTrace();
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PostMapping("/addUser")
    public ResponseEntity<Void> addUser(@RequestBody AppUserDTO appUserDTO){
        AppUser appUser = appUserMapper.mapUserDTO(appUserDTO);
        appUserDAO.save(appUser);
        return ResponseEntity.ok().build();
    }

}
