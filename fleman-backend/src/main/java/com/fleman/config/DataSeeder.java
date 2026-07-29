package com.fleman.config;

import com.fleman.entity.*;
import com.fleman.entity.BookingHeader.BookingStatus;
import com.fleman.entity.Car.CarStatus;
import com.fleman.entity.Car.FuelType;
import com.fleman.entity.Customer.CustomerStatus;
import com.fleman.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Populates the database with India-based fixture data, weighted toward
 * Maharashtra — 5 states, 20 cities (8 of them Maharashtra), 9 city hubs
 * plus 8 airport kiosks, 6 car types priced in INR, an original hand-picked
 * fleet of 16 cars (14 of them at Maharashtra hubs) topped up with enough
 * generated cars that every one of the 17 hubs — including every airport
 * kiosk — has at least one AVAILABLE car of every car type, 5 add-ons, one
 * sample customer, and the one sample WDR-2894 booking that
 * StaffReturnPage/ModifyCancelPage default their confirmation-number field to.
 *
 * No entity relationships are used anywhere in this project — every
 * cross-table reference here is a plain id assigned by hand, matching the
 * DB design's "Logical Ref" pattern throughout (not just the few fields
 * the sheet originally marked "fk").
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final HubRepository hubRepository;
    private final AirportRepository airportRepository;
    private final CarTypeRepository carTypeRepository;
    private final CarRepository carRepository;
    private final AddonRepository addonRepository;
    private final CustomerRepository customerRepository;
    private final BookingHeaderRepository bookingHeaderRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<Integer, Long> stateIds = new HashMap<>();
    private final Map<Integer, Long> cityIds = new HashMap<>();
    private final Map<Integer, Long> hubIds = new HashMap<>();
    private final Map<Integer, Long> carTypeIds = new HashMap<>();
    private final Map<Integer, Long> addonIds = new HashMap<>();

    public DataSeeder(StateRepository stateRepository, CityRepository cityRepository,
                       HubRepository hubRepository, AirportRepository airportRepository,
                       CarTypeRepository carTypeRepository, CarRepository carRepository,
                       AddonRepository addonRepository, CustomerRepository customerRepository,
                       BookingHeaderRepository bookingHeaderRepository,
                       BookingDetailRepository bookingDetailRepository,
                       PasswordEncoder passwordEncoder) {
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.hubRepository = hubRepository;
        this.airportRepository = airportRepository;
        this.carTypeRepository = carTypeRepository;
        this.carRepository = carRepository;
        this.addonRepository = addonRepository;
        this.customerRepository = customerRepository;
        this.bookingHeaderRepository = bookingHeaderRepository;
        this.bookingDetailRepository = bookingDetailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (stateRepository.count() > 0) return;

        seedStates();
        seedCities();
        seedHubs();
        seedAirportsAndKioskHubs();
        seedCarTypes();
        seedCars();
        seedFullHubTypeCoverage();
        seedAddons();
        seedCustomerAndSampleBooking();
    }

    private void seedStates() {
        // Index 1 = Maharashtra, deliberately first — most cities, hubs,
        // and cars below belong to it.
        String[] names = { "Maharashtra", "Karnataka", "Delhi", "Gujarat", "Tamil Nadu" };
        for (int i = 0; i < names.length; i++) {
            State s = new State(names[i]);
            stateIds.put(i + 1, stateRepository.save(s).getStateId());
        }
    }

    private void seedCities() {
        // { cityId (local index), cityName, stateId (local index) }
        Object[][] rows = {
                // Maharashtra — 8 cities, the bulk of the map
                {1, "Mumbai", 1}, {2, "Pune", 1}, {3, "Nagpur", 1}, {4, "Nashik", 1},
                {5, "Thane", 1}, {6, "Navi Mumbai", 1}, {7, "Chhatrapati Sambhajinagar", 1}, {8, "Kolhapur", 1},
                // Karnataka
                {9, "Bengaluru", 2}, {10, "Mysuru", 2}, {11, "Hubballi", 2},
                // Delhi
                {12, "New Delhi", 3}, {13, "Dwarka", 3}, {14, "Rohini", 3},
                // Gujarat
                {15, "Ahmedabad", 4}, {16, "Surat", 4}, {17, "Vadodara", 4},
                // Tamil Nadu
                {18, "Chennai", 5}, {19, "Coimbatore", 5}, {20, "Madurai", 5},
        };
        for (Object[] r : rows) {
            City c = new City((String) r[1], stateIds.get((Integer) r[2]));
            cityIds.put((Integer) r[0], cityRepository.save(c).getCityId());
        }
    }

    private void seedHubs() {
        // { hubId (local index), hubName, address, cityId (local), stateId (local),
        //   pincode, contactNo, email }
        Object[][] rows = {
                {1, "WanderCar — Mumbai Hub", "Plot 14, Andheri-Kurla Road", 1, 1, "400059", "+91 22 4001 5566", "mumbai.hub@wandercar.example"},
                {2, "WanderCar — Pune Hub", "221, FC Road, Shivajinagar", 2, 1, "411005", "+91 20 4002 5566", "pune.hub@wandercar.example"},
                {3, "WanderCar — Nagpur Hub", "45, Wardha Road", 3, 1, "440012", "+91 712 400 5566", "nagpur.hub@wandercar.example"},
                {4, "WanderCar — Nashik Hub", "12, College Road", 4, 1, "422005", "+91 253 400 5566", "nashik.hub@wandercar.example"},
                {5, "WanderCar — Thane Hub", "8, Ghodbunder Road", 5, 1, "400607", "+91 22 4003 5566", "thane.hub@wandercar.example"},
                {6, "WanderCar — Bengaluru Hub", "77, Outer Ring Road", 9, 2, "560103", "+91 80 4004 5566", "bengaluru.hub@wandercar.example"},
                {7, "WanderCar — New Delhi Hub", "19, Connaught Place", 12, 3, "110001", "+91 11 4005 5566", "delhi.hub@wandercar.example"},
                {8, "WanderCar — Ahmedabad Hub", "5, SG Highway", 15, 4, "380015", "+91 79 4006 5566", "ahmedabad.hub@wandercar.example"},
                {9, "WanderCar — Chennai Hub", "31, Anna Salai", 18, 5, "600002", "+91 44 4007 5566", "chennai.hub@wandercar.example"},
        };
        for (Object[] r : rows) {
            hubIds.put((Integer) r[0], saveHub((String) r[1], (String) r[2], (Integer) r[3], (Integer) r[4],
                    (String) r[5], (String) r[6], (String) r[7]));
        }
    }

    private void seedAirportsAndKioskHubs() {
        // { airportId, code, name, cityId (local), stateId (local), kioskHubId (local) }
        // Weighted Maharashtra: 4 of the 8 airports.
        Object[][] rows = {
                {1, "BOM", "Chhatrapati Shivaji Maharaj International Airport", 1, 1, 10},
                {2, "PNQ", "Pune Airport", 2, 1, 11},
                {3, "NAG", "Dr. Babasaheb Ambedkar International Airport", 3, 1, 12},
                {4, "ISK", "Nashik Airport", 4, 1, 13},
                {5, "BLR", "Kempegowda International Airport", 9, 2, 14},
                {6, "DEL", "Indira Gandhi International Airport", 12, 3, 15},
                {7, "AMD", "Sardar Vallabhbhai Patel International Airport", 15, 4, 16},
                {8, "MAA", "Chennai International Airport", 18, 5, 17},
        };
        for (Object[] r : rows) {
            int cityId = (Integer) r[3];
            int stateId = (Integer) r[4];
            int kioskHubId = (Integer) r[5];
            String code = (String) r[1];
            String name = (String) r[2];

            Long kioskHub = saveHub(
                    "WanderCar — " + code + " Airport Kiosk",
                    name + " . Rental Car Counter",
                    cityId, stateId, "000000", "1800-123-5566", "airport.kiosk@wandercar.example"
            );
            hubIds.put(kioskHubId, kioskHub);

            Airport airport = new Airport();
            airport.setAirportCode(code);
            airport.setAirportName(name);
            airport.setCityId(cityIds.get(cityId));
            airport.setStateId(stateIds.get(stateId));
            airport.setHubId(kioskHub);
            airportRepository.save(airport);
        }
    }

    private Long saveHub(String name, String address, int cityId, int stateId,
                         String pincode, String contactNo, String email) {
        Hub h = new Hub();
        h.setHubName(name);
        h.setAddress(address);
        h.setCityId(cityIds.get(cityId));
        h.setStateId(stateIds.get(stateId));
        h.setPincode(pincode);
        h.setContactNo(contactNo);
        h.setEmail(email);
        return hubRepository.save(h).getHubId();
    }

    private void seedCarTypes() {
        // { id, name, daily (INR), weekly, monthly, image }
        Object[][] rows = {
                {1, "Economy", 1500.0, 7000.0, 28000.0, "/cars/economy.svg"},
                {2, "Compact", 2200.0, 13200.0, 47000.0, "/cars/compact.svg"},
                {3, "Sedan", 3000.0, 18000.0, 64000.0, "/cars/sedan.svg"},
                {4, "SUV", 4200.0, 25200.0, 90000.0, "/cars/suv.svg"},
                {5, "Luxury", 7500.0, 45000.0, 160000.0, "/cars/luxury.svg"},
                {6, "Minivan", 3600.0, 21600.0, 77000.0, "/cars/minivan.svg"},
        };
        for (Object[] r : rows) {
            CarType ct = new CarType();
            ct.setCarTypeName((String) r[1]);
            ct.setDailyRate((Double) r[2]);
            ct.setWeeklyRate((Double) r[3]);
            ct.setMonthlyRate((Double) r[4]);
            ct.setRateValidFrom(LocalDate.of(2026, 1, 1));
            ct.setRateValidTo(LocalDate.of(2026, 12, 31));
            ct.setImagePath((String) r[5]);
            carTypeIds.put((Integer) r[0], carTypeRepository.save(ct).getCarTypeId());
        }
    }

    private void seedCars() {
        // { carId, carTypeId, hubId, vehicleNumber, brand, model, year, color,
        //   fuelType, seats, mileage(kmpl), odometer, fuelLevel, isAvailable, status }
        // 14 of 16 cars sit at Maharashtra hubs (1-5); one each at Bengaluru and Ahmedabad.
        Object[][] rows = {
                {1, 1, 1, "MH01AB1234", "Maruti Suzuki", "Swift", 2024, "White", "PETROL", 4, 22.5, 18240, 82, true, "AVAILABLE"},
                {2, 2, 1, "MH01AC2345", "Hyundai", "i20", 2025, "Silver", "PETROL", 5, 18.0, 9120, 50, true, "AVAILABLE"},
                {3, 3, 1, "MH01AD3456", "Honda", "City", 2025, "Grey", "PETROL", 5, 17.8, 4210, 95, true, "AVAILABLE"},
                {4, 4, 1, "MH01AE4567", "Mahindra", "XUV700", 2024, "Black", "DIESEL", 7, 14.5, 22890, 40, false, "BOOKED"},
                {5, 5, 1, "MH01AF5678", "Mercedes-Benz", "E-Class", 2025, "Black", "PETROL", 5, 12.0, 3100, 100, true, "AVAILABLE"},
                {6, 4, 2, "MH12BG6789", "Tata", "Nexon EV", 2024, "Blue", "ELECTRIC", 5, 0.0, 6100, 88, true, "AVAILABLE"},
                {7, 1, 2, "MH12BH7890", "Maruti Suzuki", "Baleno", 2024, "Red", "PETROL", 5, 21.0, 27650, 55, true, "AVAILABLE"},
                {8, 6, 2, "MH12BI8901", "Toyota", "Innova Crysta", 2023, "White", "DIESEL", 7, 11.5, 41800, 60, true, "UNDER_MAINTENANCE"},
                {9, 4, 3, "MH31CJ9012", "Kia", "Seltos", 2024, "Grey", "PETROL", 5, 16.5, 12200, 70, true, "AVAILABLE"},
                {10, 3, 3, "MH31CK0123", "Hyundai", "Verna", 2025, "White", "PETROL", 5, 18.4, 2200, 90, true, "AVAILABLE"},
                {11, 5, 4, "MH15DL1234", "BMW", "5 Series", 2025, "Black", "PETROL", 5, 13.0, 8700, 75, true, "AVAILABLE"},
                {12, 1, 4, "MH15DM2345", "Maruti Suzuki", "Alto", 2023, "Silver", "PETROL", 4, 24.0, 38900, 45, true, "AVAILABLE"},
                {13, 4, 5, "MH04EN3456", "Toyota", "Fortuner", 2024, "White", "DIESEL", 7, 10.5, 17600, 60, true, "AVAILABLE"},
                {14, 2, 5, "MH04EO4567", "Hyundai", "Grand i10 Nios", 2024, "Blue", "PETROL", 5, 20.3, 5400, 85, true, "AVAILABLE"},
                {15, 3, 6, "KA01FP5678", "Honda", "Amaze", 2024, "Grey", "PETROL", 5, 19.2, 9800, 65, true, "AVAILABLE"},
                {16, 1, 8, "GJ01GQ6789", "Tata", "Tiago", 2023, "Red", "PETROL", 5, 23.8, 14300, 70, true, "AVAILABLE"},
        };
        for (Object[] r : rows) {
            Car car = new Car();
            car.setCarTypeId(carTypeIds.get((Integer) r[1]));
            car.setHubId(hubIds.get((Integer) r[2]));
            car.setVehicleNumber((String) r[3]);
            car.setBrand((String) r[4]);
            car.setModel((String) r[5]);
            car.setManufactureYear((Integer) r[6]);
            car.setColor((String) r[7]);
            car.setFuelType(FuelType.valueOf((String) r[8]));
            car.setSeatingCapacity((Integer) r[9]);
            car.setMileage((Double) r[10]);
            car.setOdometer((Integer) r[11]);
            car.setFuelLevel((Integer) r[12]);
            car.setAvailable((Boolean) r[13]);
            car.setStatus(CarStatus.valueOf((String) r[14]));
            carRepository.save(car);
        }
    }

    // Every (hubId, carTypeId) pair the hand-picked fleet above already
    // covers with a genuinely AVAILABLE car — i.e. NOT car #4 (SUV at
    // Mumbai, BOOKED) or car #8 (Minivan at Pune, UNDER_MAINTENANCE).
    // Anything not in this set gets a generated filler car below, so every
    // hub — city hub or airport kiosk alike — has all 6 car types to show
    // on the vehicle-selection page.
    private static final java.util.Set<String> HAND_PICKED_COVERAGE = java.util.Set.of(
            "1-1", "1-2", "1-3", "1-5",
            "2-1", "2-4",
            "3-3", "3-4",
            "4-1", "4-5",
            "5-2", "5-4",
            "6-3",
            "8-1"
    );

    // { carTypeId, brand, model, fuelType, seats, mileage }
    private static final Object[][] TYPE_DEFAULTS = {
            {1, "Maruti Suzuki", "Alto", "PETROL", 4, 24.0},
            {2, "Hyundai", "i20", "PETROL", 5, 20.0},
            {3, "Honda", "City", "PETROL", 5, 18.0},
            {4, "Mahindra", "XUV700", "DIESEL", 7, 15.0},
            {5, "Mercedes-Benz", "E-Class", "PETROL", 5, 12.0},
            {6, "Toyota", "Innova Crysta", "DIESEL", 7, 12.0},
    };

    // Fills in every (hub, carType) combination the hand-picked fleet in
    // seedCars() doesn't already cover with two AVAILABLE cars - most
    // importantly the 8 airport kiosk hubs, which otherwise have zero cars
    // at all and would show an empty vehicle-selection page the moment a
    // customer picks an airport for pickup instead of a city. Two per
    // combination (not one) so a single booking doesn't immediately drop
    // that car type off the vehicle-selection page for everyone else - it
    // only disappears once every car of that type at that hub is genuinely
    // taken for the requested dates.
    private void seedFullHubTypeCoverage() {
        int vehicleNumberSeq = 17; // continues on from the hand-picked fleet's 16 cars
        for (Integer hubIndex : hubIds.keySet()) {
            for (Object[] typeDefault : TYPE_DEFAULTS) {
                int typeIndex = (Integer) typeDefault[0];
                if (HAND_PICKED_COVERAGE.contains(hubIndex + "-" + typeIndex)) continue;

                for (int copy = 0; copy < 2; copy++) {
                    Car car = new Car();
                    car.setCarTypeId(carTypeIds.get(typeIndex));
                    car.setHubId(hubIds.get(hubIndex));
                    car.setVehicleNumber("WCR" + String.format("%04d", vehicleNumberSeq++));
                    car.setBrand((String) typeDefault[1]);
                    car.setModel((String) typeDefault[2]);
                    car.setManufactureYear(2025);
                    car.setColor("White");
                    car.setFuelType(FuelType.valueOf((String) typeDefault[3]));
                    car.setSeatingCapacity((Integer) typeDefault[4]);
                    car.setMileage((Double) typeDefault[5]);
                    car.setOdometer(0);
                    car.setFuelLevel(100);
                    car.setAvailable(true);
                    car.setStatus(CarStatus.AVAILABLE);
                    carRepository.save(car);
                }
            }
        }
    }

    private void seedAddons() {
        // { id, name, dailyRate (INR), description }
        // Additional Driver and Roadside Assistance Plus were removed from
        // the catalog per product decision - GPS Navigation and WiFi
        // Hotspot are one-per-booking toggles, Child Seat is the only
        // add-on with a quantity stepper (0-3, enforced on the add-ons page).
        Object[][] rows = {
                {1, "GPS Navigation", 250.0, "Turn-by-turn navigation unit, mounted and ready."},
                {2, "Child Seat", 300.0, "Rear-facing or booster, installed at pickup."},
                {5, "WiFi Hotspot", 300.0, "In-car 4G hotspot, unlimited data."},
        };
        for (Object[] r : rows) {
            Addon a = new Addon();
            a.setAddonName((String) r[1]);
            a.setDailyRate((Double) r[2]);
            a.setRateValidFrom(LocalDate.of(2026, 1, 1));
            a.setRateValidTo(LocalDate.of(2026, 12, 31));
            a.setDescription((String) r[3]);
            addonIds.put((Integer) r[0], addonRepository.save(a).getAddonId());
        }
    }

    private void seedCustomerAndSampleBooking() {
        Customer customer = new Customer();
        customer.setFullName("Rohan Deshmukh");
        customer.setEmail("rohan.deshmukh@email.com");
        customer.setPhone("9876543210");
        customer.setDateOfBirth(LocalDate.of(1994, 5, 12));
        customer.setDrivingLicenseNo("MH1220230012345");
        customer.setAddress1("221, FC Road, Shivajinagar");
        customer.setAddress2("");
        customer.setCityId(cityIds.get(2));   // Pune
        customer.setStateId(stateIds.get(1)); // Maharashtra
        customer.setPincode("411005");
        customer.setStatus(CustomerStatus.ACTIVE);
        // Same example password as before ('password123'), now properly hashed.
        customer.setPasswordHash(passwordEncoder.encode("password123"));
        customer = customerRepository.save(customer);

        BookingHeader header = new BookingHeader();
        header.setConfirmationNo("WDR-2894");
        header.setBookingDate(LocalDateTime.of(2026, 7, 18, 9, 30));
        header.setCustomerId(customer.getCustomerId());
        header.setCarId(2L); // Hyundai i20, Compact, Mumbai hub
        header.setPickupHubId(hubIds.get(1)); // Mumbai
        header.setDropHubId(hubIds.get(1));
        header.setPickupDatetime(LocalDateTime.of(2026, 7, 18, 10, 0));
        header.setReturnDatetime(LocalDateTime.of(2026, 7, 21, 10, 0));
        header.setBookingStatus(BookingStatus.CONFIRMED);
        // 3 days x Compact daily rate (2200) = 6600, + 1 GPS Navigation x 3 days (250 x 1 x 3 = 750)
        header.setEstimatedAmount(7350.0);
        header.setRemarks("");
        header = bookingHeaderRepository.save(header);

        BookingDetail detail = new BookingDetail();
        detail.setBookingId(header.getBookingId());
        detail.setAddonId(addonIds.get(1)); // GPS Navigation
        detail.setAddonRate(250.0);
        detail.setQuantity(1);
        detail.setSubtotal(750.0);
        bookingDetailRepository.save(detail);
    }
}
