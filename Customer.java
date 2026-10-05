public class Customer implements Cloneable {
    private static long customerCounter = 101;

    private final String customerId;
    private String name;
    private String email;
    private String mobile;
    private Address address;

    // Requirement 3: Public static nested class named Address
    public static class Address {
        private String line;
        private String city;
        private String pincode;

        public Address(String line, String city, String pincode) {
            this.line = line;
            this.city = city;
            this.pincode = pincode;
        }

        public String getLine() {
            return line;
        }

        public String getCity() {
            return city;
        }

        public String getPincode() {
            return pincode;
        }

        @Override
        public String toString() {
            return line + ", " + city + " - " + pincode;
        }
    }

    private static String generateCustomerId() {
        return "CUST" + customerCounter++;
    }

    public Customer(String name, String email, String mobile) {
        this(name, email, mobile, null);
    }

    public Customer(String name, String email, String mobile, Address address) {
        this.customerId = generateCustomerId();
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.address = address;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    // Requirement 3: getAddress() method
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    // Requirement 4: clone() method implementing Cloneable
    @Override
    public Customer clone() {
        try {
            Customer copy = (Customer) super.clone();
            if (this.address != null) {
                copy.address = new Address(this.address.getLine(), this.address.getCity(), this.address.getPincode());
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

    @Override
    public String toString() {
        return "Customer [ID: " + customerId + ", Name: " + name + ", Email: " + email + ", Mobile: " + mobile +
                (address != null ? ", Address: (" + address + ")" : "") + "]";
    }
}
