package com.buseasy.util;

import java.util.HashMap;
import java.util.Map;

public final class LanguageManager {

    public enum Language {
        EN("EN", "English"),
        VI("VI", "VI");

        private final String code;
        private final String label;

        Language(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String code() {
            return code;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static Language current = Language.EN;
    private static final Map<String, String> EN = new HashMap<>();
    private static final Map<String, String> VI = new HashMap<>();

    static {
        put("logout", "Logout", "\u0110\u0103ng xu\u1ea5t");
        put("welcome", "Welcome, ", "Xin ch\u00e0o, ");
        put("home", "Home", "Trang ch\u1ee7");
        put("cart", "Cart", "Gi\u1ecf v\u00e9");
        put("profile", "Profile", "H\u1ed3 s\u01a1");
        put("history", "History", "L\u1ecbch s\u1eed");
        put("admin.console", "Admin Console", "B\u1ea3ng qu\u1ea3n tr\u1ecb");
        put("notifications", "Notifications", "Th\u00f4ng b\u00e1o");
        put("language", "Language", "Ng\u00f4n ng\u1eef");
        put("refresh.all", "Refresh all", "L\u00e0m m\u1edbi t\u1ea5t c\u1ea3");
        put("refreshing", "Refreshing...", "\u0110ang l\u00e0m m\u1edbi...");
        put("dashboard", "Dashboard", "T\u1ed5ng quan");
        put("schedules", "Schedules", "Chuy\u1ebfn xe");
        put("users", "Users", "Ng\u01b0\u1eddi d\u00f9ng");
        put("tickets", "Tickets", "V\u00e9");
        put("military.requests", "Military Requests", "Y\u00eau c\u1ea7u qu\u00e2n nh\u00e2n");
        put("search.route.bus", "Search route or bus", "T\u00ecm tuy\u1ebfn ho\u1eb7c xe");
        put("search.user", "Search user", "T\u00ecm ng\u01b0\u1eddi d\u00f9ng");
        put("search.ticket", "Search ticket", "T\u00ecm v\u00e9");
        put("search.request", "Search request", "T\u00ecm y\u00eau c\u1ea7u");
        put("all.trips", "All trips", "T\u1ea5t c\u1ea3 chuy\u1ebfn");
        put("seats.available", "Seats available", "C\u00f2n gh\u1ebf");
        put("low.seats", "Low seats", "\u00cdt gh\u1ebf");
        put("date", "Date", "Ng\u00e0y");
        put("go", "Go", "\u0110i");
        put("previous", "< Previous", "< Tr\u01b0\u1edbc");
        put("next", "Next >", "Sau >");
        put("past.date", "Cannot view a past date", "Kh\u00f4ng th\u1ec3 xem ng\u00e0y trong qu\u00e1 kh\u1ee9");
        put("no.trips.day", "No trips on ", "Kh\u00f4ng c\u00f3 chuy\u1ebfn n\u00e0o trong ng\u00e0y ");
        put("invalid.date", "Invalid date.", "Ng\u00e0y kh\u00f4ng h\u1ee3p l\u1ec7.");
        put("military.form.title", "Military discount request", "\u0110\u0103ng k\u00fd \u01b0u \u0111\u00e3i qu\u00e2n nh\u00e2n");
        put("military.service.no", "Service number", "M\u00e3 qu\u00e2n nh\u00e2n");
        put("military.unit", "Unit / agency", "\u0110\u01a1n v\u1ecb c\u00f4ng t\u00e1c");
        put("military.note", "Note", "Ghi ch\u00fa");
        put("military.submit", "Submit request", "G\u1eedi y\u00eau c\u1ea7u");
        put("military.pending", "Military request submitted. Please wait for admin review.",
            "\u0110\u00e3 g\u1eedi y\u00eau c\u1ea7u qu\u00e2n nh\u00e2n. Vui l\u00f2ng ch\u1edd qu\u1ea3n tr\u1ecb vi\u00ean duy\u1ec7t.");
        put("military.approved", "Your military discount request was approved.",
            "Y\u00eau c\u1ea7u \u01b0u \u0111\u00e3i qu\u00e2n nh\u00e2n c\u1ee7a b\u1ea1n \u0111\u00e3 \u0111\u01b0\u1ee3c duy\u1ec7t.");
        put("military.denied", "Your military discount request was denied.",
            "Y\u00eau c\u1ea7u \u01b0u \u0111\u00e3i qu\u00e2n nh\u00e2n c\u1ee7a b\u1ea1n \u0111\u00e3 b\u1ecb t\u1eeb ch\u1ed1i.");
        put("payment.success", "Payment successful", "Thanh to\u00e1n th\u00e0nh c\u00f4ng");
        put("payment.failed", "Payment failed", "Thanh to\u00e1n th\u1ea5t b\u1ea1i");
        put("no.notifications", "No notifications yet.", "Ch\u01b0a c\u00f3 th\u00f4ng b\u00e1o.");

        put("URBAN MOBILITY PLATFORM", "URBAN MOBILITY PLATFORM", "N\u1ec0N T\u1ea2NG DI CHUY\u1ec2N \u0110\u00d4 TH\u1eca");
        put("Book your next route in seconds", "Book your next route in seconds", "\u0110\u1eb7t chuy\u1ebfn ti\u1ebfp theo trong v\u00e0i gi\u00e2y");
        put("Sign in to browse routes, manage tickets, and check out faster.",
            "Sign in to browse routes, manage tickets, and check out faster.",
            "\u0110\u0103ng nh\u1eadp \u0111\u1ec3 xem tuy\u1ebfn, qu\u1ea3n l\u00fd v\u00e9 v\u00e0 thanh to\u00e1n nhanh h\u01a1n.");
        put("Username", "Username", "T\u00ean \u0111\u0103ng nh\u1eadp");
        put("Password", "Password", "M\u1eadt kh\u1ea9u");
        put("Login", "Login", "\u0110\u0103ng nh\u1eadp");
        put("Create an account", "Create an account", "T\u1ea1o t\u00e0i kho\u1ea3n");
        put("FIRST TRIP, ZERO FRICTION", "FIRST TRIP, ZERO FRICTION", "CHUY\u1ebeN \u0110\u1ea6U TI\u00caN, D\u1ec4 D\u00c0NG");
        put("Create your BusEasy account", "Create your BusEasy account", "T\u1ea1o t\u00e0i kho\u1ea3n BusEasy");
        put("Save your profile once, then book routes and manage tickets from one place.",
            "Save your profile once, then book routes and manage tickets from one place.",
            "L\u01b0u h\u1ed3 s\u01a1 m\u1ed9t l\u1ea7n, sau \u0111\u00f3 \u0111\u1eb7t chuy\u1ebfn v\u00e0 qu\u1ea3n l\u00fd v\u00e9 t\u1ea1i m\u1ed9t n\u01a1i.");
        put("Full Name *", "Full Name *", "H\u1ecd t\u00ean *");
        put("Username *", "Username *", "T\u00ean \u0111\u0103ng nh\u1eadp *");
        put("Password *", "Password *", "M\u1eadt kh\u1ea9u *");
        put("Email *", "Email *", "Email *");
        put("Phone", "Phone", "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i");
        put("Register", "Register", "\u0110\u0103ng k\u00fd");
        put("Back to login", "Back to login", "Quay l\u1ea1i \u0111\u0103ng nh\u1eadp");
        put("Military personnel", "Military personnel", "Qu\u00e2n nh\u00e2n");

        put("My Cart", "My Cart", "Gi\u1ecf v\u00e9 c\u1ee7a t\u00f4i");
        put("Checkout", "Checkout", "Thanh to\u00e1n");
        put("Your cart is empty.", "Your cart is empty.", "Gi\u1ecf v\u00e9 \u0111ang tr\u1ed1ng.");
        put("Adult", "Adult", "Ng\u01b0\u1eddi l\u1edbn");
        put("Child", "Child", "Tr\u1ebb em");
        put("Military", "Military", "Qu\u00e2n nh\u00e2n");
        put("Remove", "Remove", "X\u00f3a");
        put("Total", "Total", "T\u1ed5ng");
        put("Confirm purchase of all items in your cart?", "Confirm purchase of all items in your cart?",
            "X\u00e1c nh\u1eadn mua t\u1ea5t c\u1ea3 chuy\u1ebfn trong gi\u1ecf?");
        put("Success", "Success", "Th\u00e0nh c\u00f4ng");
        put("Booking Complete", "Booking Complete", "\u0110\u1eb7t v\u00e9 ho\u00e0n t\u1ea5t");

        put("Set Reminder", "Set Reminder", "\u0110\u1eb7t nh\u1eafc nh\u1edf");
        put("BOOKING COMPLETE", "BOOKING COMPLETE", "\u0110\u1eb6T V\u00c9 HO\u00c0N T\u1ea4T");
        put("Choose a reminder for this checkout", "Choose a reminder for this checkout", "Ch\u1ecdn nh\u1eafc nh\u1edf cho l\u1ea7n thanh to\u00e1n n\u00e0y");
        put("This reminder will apply to all tickets bought just now.", "This reminder will apply to all tickets bought just now.",
            "Nh\u1eafc nh\u1edf n\u00e0y s\u1ebd \u00e1p d\u1ee5ng cho t\u1ea5t c\u1ea3 v\u00e9 v\u1eeba mua.");
        put("Purchased tickets", "Purchased tickets", "V\u00e9 \u0111\u00e3 mua");
        put("Reminder time", "Reminder time", "Th\u1eddi gian nh\u1eafc");
        put("Skip", "Skip", "B\u1ecf qua");
        put("Save Reminder", "Save Reminder", "L\u01b0u nh\u1eafc nh\u1edf");
        put("No reminder", "No reminder", "Kh\u00f4ng nh\u1eafc");
        put("15 minutes before", "15 minutes before", "Tr\u01b0\u1edbc 15 ph\u00fat");
        put("30 minutes before", "30 minutes before", "Tr\u01b0\u1edbc 30 ph\u00fat");
        put("1 hour before", "1 hour before", "Tr\u01b0\u1edbc 1 gi\u1edd");
        put("2 hours before", "2 hours before", "Tr\u01b0\u1edbc 2 gi\u1edd");
        put("1 day before", "1 day before", "Tr\u01b0\u1edbc 1 ng\u00e0y");
        put("2 days before", "2 days before", "Tr\u01b0\u1edbc 2 ng\u00e0y");

        put("Add to Cart", "Add to Cart", "Th\u00eam v\u00e0o gi\u1ecf");
        put("ADD TRIP TO CART", "ADD TRIP TO CART", "TH\u00caM CHUY\u1ebeN V\u00c0O GI\u1ece");
        put("Adult passengers", "Adult passengers", "H\u00e0nh kh\u00e1ch ng\u01b0\u1eddi l\u1edbn");
        put("Child passengers", "Child passengers", "H\u00e0nh kh\u00e1ch tr\u1ebb em");
        put("Standard fare", "Standard fare", "Gi\u00e1 v\u00e9 chu\u1ea9n");
        put("50% of adult fare", "50% of adult fare", "50% gi\u00e1 v\u00e9 ng\u01b0\u1eddi l\u1edbn");
        put("Military discount (20% off)", "Military discount (20% off)", "\u01afu \u0111\u00e3i qu\u00e2n nh\u00e2n (gi\u1ea3m 20%)");
        put("Estimated total", "Estimated total", "T\u1ed5ng t\u1ea1m t\u00ednh");
        put("Cancel", "Cancel", "H\u1ee7y");
        put("Select at least one passenger.", "Select at least one passenger.", "Ch\u1ecdn \u00edt nh\u1ea5t m\u1ed9t h\u00e0nh kh\u00e1ch.");
        put("Departs", "Departs", "Kh\u1edfi h\u00e0nh");
        put("Arrives", "Arrives", "\u0110\u1ebfn n\u01a1i");

        put("Choose a Bus", "Choose a Bus", "Ch\u1ecdn xe");
        put("Multiple buses at this time - choose one:", "Multiple buses at this time - choose one:", "C\u00f3 nhi\u1ec1u xe c\u00f9ng gi\u1edd - ch\u1ecdn m\u1ed9t xe:");
        put("Pick this bus", "Pick this bus", "Ch\u1ecdn xe n\u00e0y");
        put("Search results for", "Search results for", "K\u1ebft qu\u1ea3 t\u00ecm ki\u1ebfm cho");
        put("Filter", "Filter", "L\u1ecdc");
        put("Add", "Add", "Th\u00eam");
        put("No matching trips.", "No matching trips.", "Kh\u00f4ng c\u00f3 chuy\u1ebfn ph\u00f9 h\u1ee3p.");
        put("seats left", "seats left", "gh\u1ebf tr\u1ed1ng");
        put("Schedules on", "Schedules on", "Chuy\u1ebfn trong ng\u00e0y");
        put("No departures scheduled for this day.", "No departures scheduled for this day.", "Kh\u00f4ng c\u00f3 chuy\u1ebfn kh\u1edfi h\u00e0nh trong ng\u00e0y n\u00e0y.");
        put("Select >", "Select >", "Ch\u1ecdn >");
        put("< Back to Calendar", "< Back to Calendar", "< Quay l\u1ea1i l\u1ecbch");

        put("My Profile", "My Profile", "H\u1ed3 s\u01a1 c\u1ee7a t\u00f4i");
        put("PROFILE SETTINGS", "PROFILE SETTINGS", "C\u00c0I \u0110\u1eb6T H\u1ed2 S\u01a0");
        put("Full Name:", "Full Name:", "H\u1ecd t\u00ean:");
        put("Username:", "Username:", "T\u00ean \u0111\u0103ng nh\u1eadp:");
        put("Email:", "Email:", "Email:");
        put("Phone:", "Phone:", "S\u1ed1 \u0111i\u1ec7n tho\u1ea1i:");
        put("Save Changes", "Save Changes", "L\u01b0u thay \u0111\u1ed5i");
        put("No military request submitted.", "No military request submitted.", "Ch\u01b0a g\u1eedi y\u00eau c\u1ea7u qu\u00e2n nh\u00e2n.");
        put("Request status", "Request status", "Tr\u1ea1ng th\u00e1i y\u00eau c\u1ea7u");
        put("Profile saved.", "Profile saved.", "H\u1ed3 s\u01a1 \u0111\u00e3 l\u01b0u.");

        put("My Ticket History", "My Ticket History", "L\u1ecbch s\u1eed v\u00e9 c\u1ee7a t\u00f4i");
        put("Valid Tickets", "Valid Tickets", "V\u00e9 c\u00f2n hi\u1ec7u l\u1ef1c");
        put("Expired Tickets", "Expired Tickets", "V\u00e9 \u0111\u00e3 h\u1ebft h\u1ea1n");
        put("No upcoming tickets.", "No upcoming tickets.", "Kh\u00f4ng c\u00f3 v\u00e9 s\u1eafp t\u1edbi.");
        put("No past tickets.", "No past tickets.", "Kh\u00f4ng c\u00f3 v\u00e9 \u0111\u00e3 qua.");
        put("Detail", "Detail", "Chi ti\u1ebft");
        put("Ticket Detail", "Ticket Detail", "Chi ti\u1ebft v\u00e9");
        put("Confirm Cancellation", "Confirm Cancellation", "X\u00e1c nh\u1eadn h\u1ee7y");
        put("Seats will be returned.", "Seats will be returned.", "Gh\u1ebf s\u1ebd \u0111\u01b0\u1ee3c ho\u00e0n l\u1ea1i.");
        put("Adults", "Adults", "Ng\u01b0\u1eddi l\u1edbn");
        put("Children", "Children", "Tr\u1ebb em");
        put("Yes", "Yes", "C\u00f3");
        put("No", "No", "Kh\u00f4ng");
        put("Status", "Status", "Tr\u1ea1ng th\u00e1i");
        put("Route", "Route", "Tuy\u1ebfn");
        put("Bus", "Bus", "Xe");
        put("Purchased", "Purchased", "\u0110\u00e3 mua");

        put("Admin workspace", "Admin workspace", "Kh\u00f4ng gian qu\u1ea3n tr\u1ecb");
        put("Monitor schedules, customers, roles, tickets, and requests from one focused console.",
            "Monitor schedules, customers, roles, tickets, and requests from one focused console.",
            "Theo d\u00f5i chuy\u1ebfn, kh\u00e1ch h\u00e0ng, vai tr\u00f2, v\u00e9 v\u00e0 y\u00eau c\u1ea7u t\u1eeb m\u1ed9t b\u1ea3ng \u0111i\u1ec1u khi\u1ec3n.");
        put("Customers", "Customers", "Kh\u00e1ch h\u00e0ng");
        put("Admins", "Admins", "Qu\u1ea3n tr\u1ecb");
        put("Upcoming trips", "Upcoming trips", "Chuy\u1ebfn s\u1eafp t\u1edbi");
        put("Revenue", "Revenue", "Doanh thu");
        put("Low seats", "Low seats", "\u00cdt gh\u1ebf");
        put("Search", "Search", "T\u00ecm");
        put("Add new trip", "Add new trip", "Th\u00eam chuy\u1ebfn m\u1edbi");
        put("Edit selected", "Edit selected", "S\u1eeda m\u1ee5c \u0111\u00e3 ch\u1ecdn");
        put("Cancel trip", "Cancel trip", "H\u1ee7y chuy\u1ebfn");
        put("Change role", "Change role", "\u0110\u1ed5i vai tr\u00f2");
        put("Approve", "Approve", "Duy\u1ec7t");
        put("Deny", "Deny", "T\u1eeb ch\u1ed1i");
    }

    private LanguageManager() {
    }

    public static Language getCurrent() {
        return current;
    }

    public static void setCurrent(Language language) {
        if (language != null) {
            current = language;
        }
    }

    public static String text(String key) {
        return (current == Language.VI ? VI : EN).getOrDefault(key, key);
    }

    private static void put(String key, String en, String vi) {
        EN.put(key, en);
        VI.put(key, vi);
    }
}
