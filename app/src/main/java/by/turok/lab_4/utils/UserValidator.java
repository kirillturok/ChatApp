package by.turok.lab_4.utils;

public final class UserValidator {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "\\+[0-9]{12}";
    private static final String FIRST_NAME_REGEX = "^[A-zА-яЁё`'.-]{1,32}$";
    private static final String LAST_NAME_REGEX = "^[A-zА-яЁё`'.-]{1,32}$";
    private static final String PASSWORD_VALIDATOR = "^\\w{6,32}$";

    public static boolean validateEmail(String email) {
        return email.matches(EMAIL_REGEX);
    }

    public static boolean validatePhone(String phone) {
        return phone.matches(PHONE_REGEX);
    }

    public static boolean validateFirstName(String firstName) {
        return firstName.matches(FIRST_NAME_REGEX);
    }

    public static boolean validateLastName(String lastName) {
        return lastName.matches(LAST_NAME_REGEX);
    }

    public static boolean validatePassword(String password) {
        return password.matches(PASSWORD_VALIDATOR);
    }
}
