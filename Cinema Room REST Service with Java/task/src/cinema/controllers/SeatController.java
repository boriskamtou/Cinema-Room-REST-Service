package cinema.controllers;

import cinema.exception.TicketException;
import cinema.exception.WrongPasswordException;
import cinema.exception.WrongTokenException;
import cinema.models.CinemaSeat;
import cinema.models.Purchase;
import cinema.models.Seat;
import cinema.utils.Utils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class SeatController {

    private static final int NUMBER_OF_ROW = 9;
    private static final int NUMBER_OF_COLUMN = 9;

    CinemaSeat cinemaSeat;

    List<Seat> seats = new ArrayList<>();
    List<Purchase> purchases = new ArrayList<>();

    {
        cinemaSeat = new CinemaSeat(NUMBER_OF_ROW, NUMBER_OF_COLUMN, seats);
    }

    {
        for (int i = 1; i <= cinemaSeat.getRows(); i++) {
            for (int j = 1; j <= cinemaSeat.getColumns(); j++) {
                seats.add(new Seat(i, j, i <= 4 ? 10 : 8, true));
            }
        }

        cinemaSeat.setSeats(seats);
    }


    @GetMapping("/seats")
    public CinemaSeat getAllSeats() {
        return cinemaSeat;
    }

    @PostMapping("/purchase")
    public Purchase paySeat(@RequestBody Seat seat) {
        Seat newSeat = new Seat(seat.getRow(), seat.getColumn(), Utils.settingPrice(seat.getRow()), false);

        for (Seat s : seats) {
            if (s.getRow() == seat.getRow() && s.getColumn() == seat.getColumn()) {
                if (!s.isAvailable()) {
                    throw new TicketException("The ticket has been already purchased!");
                } else {
                    s.setAvailable(false);
                }
            }
        }
        if (newSeat.getRow() < 0 || newSeat.getRow() > NUMBER_OF_ROW || newSeat.getColumn() < 0 || newSeat.getColumn() > NUMBER_OF_COLUMN) {
            throw new TicketException("The number of a row or a column is out of bounds!");
        }
        UUID uuid = UUID.randomUUID();
        Purchase purchase = new Purchase(uuid.toString(), newSeat);
        purchases.add(purchase);
        return purchase;
    }

    @PostMapping("/return")
    public Map<String, Seat> returnTicket(@RequestBody Map<String, String> token) {
        for (var purchase : purchases) {
            if (purchase.getToken().equals(token.get("token"))) {
                for (var seat : seats) {
                    if(seat.equals(purchase.getTicket())) {
                        seat.setAvailable(true);
                    }
                }
                purchases.remove(purchase);
                return Map.of("ticket", purchase.getTicket());
            }
        }
        throw new WrongTokenException("Wrong token!");
    }

    @GetMapping("/stats")
    public Map<String, Integer> getStats(@RequestParam(required = false) String password) {
        Map<String, Integer> mapResponse = new LinkedHashMap<>();
        int numPurchase = purchases.size();
        int totalIncome = 0;
        for (var purchase: purchases) {
            totalIncome += purchase.getTicket().getPrice();
        }
        int totalPlaceAvailable = 0;
        for (var seat : cinemaSeat.getSeats()) {
            if(seat.isAvailable()) {
                totalPlaceAvailable += 1;
            }
        }
        if(password != null && password.equals("super_secret")) {
            mapResponse.put("income", totalIncome);
            mapResponse.put("available", totalPlaceAvailable);
            mapResponse.put("purchased", numPurchase);
            return mapResponse;
        } else {
            throw new WrongPasswordException("The password is wrong!");
        }
    }
}
