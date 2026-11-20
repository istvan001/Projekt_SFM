import com.example.mozi.model.*;

public class Main {

    public static void main(String[] args) {


        Room room = new Room(1);
        for (int i = 1; i <= 10; i++)
        {
            for(int j = 1; j <= 10; j++)
            {
                room.getAvailableseats().add(new Seat(i,j));
            }
        }
        Movie movie = new Movie.Builder("Herkules","Babi", "1:30","Adventure").isForAdultsOnly(true).roomNumber(room).build();
        Ticket ticket = new Ticket(movie,movie.getRoomNumber(),room.getAvailableseats().get(0),4343);
        Customer customer = new Customer.Builder("Bence","kerekesbence2002@gmail.com","301243384").build();
        customer.addTicket(ticket);
        System.out.println(customer.getTicket());
    }
}
