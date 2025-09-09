import java.util.*;


class Passenger {
    private String paxId;
    private String name;
    private String flightNo;
    private String contact;
    private List<Claim> claims;

    public Passenger(String paxId, String name, String flightNo, String contact) {
        this.paxId = paxId;
        this.name = name;
        this.flightNo = flightNo;
        this.contact = contact;
        this.claims = new ArrayList<>();
    }

    
    public String getPaxId() { return paxId; }
    public String getName() { return name; }
    public String getFlightNo() { return flightNo; }
    public String getContact() { return contact; }
    public List<Claim> getClaims() { return claims; }

    public void addClaim(Claim claim) {
        claims.add(claim);
    }

    public void printClaims() {
        if (claims.isEmpty()) {
            System.out.println("No claims filed by " + name);
        } else {
            System.out.println("Claims filed by " + name + ":");
            for (Claim c : claims) {
                System.out.println("  - " + c);
            }
        }
    }
}


class Baggage {
    private String bagTag;
    private double weight;
    private Passenger owner;
    private List<Checkpoint> route;
    private String status;

    public Baggage(String bagTag, double weight, Passenger owner) {
        this.bagTag = bagTag;
        this.weight = weight;
        this.owner = owner;
        this.route = new ArrayList<>();
        this.status = "Created";
    }

    
    public String getBagTag() { return bagTag; }
    public double getWeight() { return weight; }
    public Passenger getOwner() { return owner; }
    public String getStatus() { return status; }

    public void addCheckpoint(Checkpoint cp) {
        this.route.add(cp);
        this.status = cp.getName();
    }

    public void printRouteHistory() {
        System.out.println("Route history of bag " + bagTag + ":");
        for (Checkpoint cp : route) {
            System.out.println("  - " + cp);
        }
    }
}


class Checkpoint {
    private String id;
    private String name;
    private Date timestamp;

    public Checkpoint(String id, String name) {
        this.id = id;
        this.name = name;
        this.timestamp = new Date();
    }

    
    public String getId() { return id; }
    public String getName() { return name; }
    public Date getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "[" + name + " at " + timestamp + "]";
    }
}


abstract class Claim {
    protected String claimId;
    protected Baggage baggage;
    protected String description;
    protected double amount;
    protected String status;

    public Claim(String claimId, Baggage baggage, String description) {
        this.claimId = claimId;
        this.baggage = baggage;
        this.description = description;
        this.status = "Pending";
    }

    public abstract void settle(); // Polymorphism via overriding

    @Override
    public String toString() {
        return "ClaimID: " + claimId + " | Bag: " + baggage.getBagTag() +
               " | Desc: " + description + " | Amount: " + amount +
               " | Status: " + status;
    }
}


class LossClaim extends Claim {
    public LossClaim(String claimId, Baggage baggage, String description) {
        super(claimId, baggage, description);
    }

    @Override
    public void settle() {
        this.amount = baggage.getWeight() * 1000; // payout logic
        this.status = "Settled (Loss)";
    }
}


class DamageClaim extends Claim {
    public DamageClaim(String claimId, Baggage baggage, String description) {
        super(claimId, baggage, description);
    }

    @Override
    public void settle() {
        this.amount = baggage.getWeight() * 500; // lower payout
        this.status = "Settled (Damage)";
    }
}


class BaggageService {
    private Map<String, Baggage> baggageMap = new HashMap<>();

    
    public void registerBag(Baggage bag) {
        baggageMap.put(bag.getBagTag(), bag);
        System.out.println("Bag " + bag.getBagTag() + " registered for " + bag.getOwner().getName());
    }

    
    public void updateMovement(String bagTag, String checkpointId, String checkpointName) {
        Baggage bag = baggageMap.get(bagTag);
        if (bag != null) {
            bag.addCheckpoint(new Checkpoint(checkpointId, checkpointName));
            System.out.println("Bag " + bagTag + " moved to " + checkpointName);
        }
    }

    public void updateMovement(String bagTag, Checkpoint cp) {
        Baggage bag = baggageMap.get(bagTag);
        if (bag != null) {
            bag.addCheckpoint(cp);
            System.out.println("Bag " + bagTag + " moved to " + cp.getName());
        }
    }

    
    public void locateBag(String bagTag) {
        Baggage bag = baggageMap.get(bagTag);
        if (bag != null) {
            System.out.println("Bag " + bagTag + " currently at: " + bag.getStatus());
        }
    }

    
    public Claim raiseClaim(String claimId, Baggage bag, String type, String desc) {
        Claim claim;
        if (type.equalsIgnoreCase("loss")) {
            claim = new LossClaim(claimId, bag, desc);
        } else {
            claim = new DamageClaim(claimId, bag, desc);
        }
        bag.getOwner().addClaim(claim);
        System.out.println("Claim raised: " + claimId + " for bag " + bag.getBagTag());
        return claim;
    }
}


public class BaggageAppMain {
    public static void main(String[] args) {
        
        Passenger p1 = new Passenger("P001", "Seshanth", "AI101", "9876543210");

        
        Baggage b1 = new Baggage("BAG101", 20.5, p1);

        
        BaggageService service = new BaggageService();
        service.registerBag(b1);

        
        service.updateMovement("BAG101", "C01", "Check-in");
        service.updateMovement("BAG101", new Checkpoint("S01", "Security"));
        service.updateMovement("BAG101", "L01", "Loading");

        
        service.locateBag("BAG101");
        b1.printRouteHistory();

        
        Claim c1 = service.raiseClaim("CL001", b1, "loss", "Bag not delivered");
        c1.settle();
        p1.printClaims();
    }
}