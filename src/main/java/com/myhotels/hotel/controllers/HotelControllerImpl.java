package com.myhotels.hotel.controllers;

import com.myhotels.hotel.dtos.AvailabilityDto;
import com.myhotels.hotel.dtos.HotelDto;
import com.myhotels.hotel.entities.Availability;
import com.myhotels.hotel.entities.Hotel;
import com.myhotels.hotel.exceptions.DataNotFoundException;
import com.myhotels.hotel.exceptions.InvalidRequestException;
import com.myhotels.hotel.mappers.AvailabilityMapper;
import com.myhotels.hotel.mappers.HotelMapper;
import com.myhotels.hotel.services.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hotels")
public class HotelControllerImpl implements HotelController {

    @Autowired
    private HotelService service;
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private AvailabilityMapper availabilityMapper;

    @GetMapping("/")
    @Override
    public ResponseEntity<List<HotelDto>> getAllActiveHotels() {
        List<Hotel> hotels = service.getAllHotelsByStatus(true);
        if (hotels == null) {
            throw new DataNotFoundException("No hotel found.");
        } else {
            List<HotelDto> hotelDtos = hotels.stream()
                    .map(h -> hotelMapper.hotelToHotelDto(h))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(hotelDtos);
        }
    }

    @GetMapping("/active/{active}")
    @Override
    public ResponseEntity<List<HotelDto>> getAllHotelsByStatus(@PathVariable(name = "active") boolean active) {
        List<Hotel> hotels = service.getAllHotelsByStatus(active);
        if (hotels == null || hotels.isEmpty()) {
            throw new DataNotFoundException("No hotel found for specified value.");
        }
        List<HotelDto> hotelDtos = hotels.stream()
                .map(h -> hotelMapper.hotelToHotelDto(h))
                .collect(Collectors.toList());
        return ResponseEntity.ok(hotelDtos);
    }

    @GetMapping("/name/{name}")
    @Override
    public ResponseEntity<HotelDto> getHotelByName(@PathVariable(name = "name") String name) {
        Hotel hotel = service.getHotelByNameAndStatus(name, true);
        if (hotel == null) {
            throw new DataNotFoundException("No active hotel found for name: " + name);
        }
        return ResponseEntity.ok(hotelMapper.hotelToHotelDto(hotel));
    }

    @GetMapping("/name/{name}/date/start/{startDate}/end/{endDate}")
    @Override
    public List<List<AvailabilityDto>> getAvailabilityByNameAndDate(@PathVariable("name") String name, @PathVariable("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate, @PathVariable("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        // TODO: Use validator for this
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Start date cannot be less than today");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidRequestException("Start date cannot be greater than endDate");
        }
        List<List<Availability>> availabilityListsList = service.getAvailabilityByNameAndDateAndStatus(name, startDate, endDate, true);
        if (availabilityListsList == null || availabilityListsList.isEmpty()) {
            throw new DataNotFoundException("Hotel name not found.");
        }
        return availabilityListsList.stream()
                .map(availabilities -> availabilities.stream()
                        .map(availability -> availabilityMapper.availabilityToAvailabilityDto(availability))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    @GetMapping("/name/{name}/roomType/{roomType}/date/start/{startDate}/end/{endDate}")
    @Override
    public List<AvailabilityDto> getAvailabilityByNameAndDateAndRoomType(@PathVariable("name") String name, @PathVariable("roomType") String roomType, @PathVariable("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate, @PathVariable("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        // TODO: Use validator for this
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Start date cannot be less than today");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidRequestException("Start date cannot be greater than endDate");
        }
        List<Availability> availabilities = service.getAvailabilityByNameAndRoomTypeAndDateAndStatus(name, roomType, startDate, endDate, true);
        if (availabilities == null || availabilities.isEmpty()) {
            throw new DataNotFoundException("Hotel name not found.");
        }
        return availabilities.stream()
                .map(availability -> availabilityMapper.availabilityToAvailabilityDto(availability))
                .collect(Collectors.toList());
    }
}
