class FormValidations {
  static final passwordRegex =
      RegExp(r'^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$');
  static final latitudeRegex =
      RegExp(r'^(\+|-)?(?:90(?:\.0{1,6})?|\d{1,2}\.[0-9]{1,6})$');
  static final longitudeRegex =
      RegExp(r'^(\+|-)?(?:180(?:\.0{1,6})?|(?:1[0-7]\d|\d{1,2})\.[0-9]{1,6})$');

  static String? validateLatitude(String? value) {
    if (value == null || value.isEmpty) {
      return 'Latitude is required';
    }
    if (!FormValidations.latitudeRegex.hasMatch(value)) {
      return 'Please enter a valid latitude';
    }
    return null;
  }

  static String? validateLongitude(String? value) {
    if (value == null || value.isEmpty) {
      return 'Longitude is required';
    }
    if (!FormValidations.longitudeRegex.hasMatch(value)) {
      return 'Please enter a valid longitude';
    }
    return null;
  }

  static String? validatePassword(
      String? value) {
    if (value == null || value.isEmpty) {
      return 'Password is required';
    }
    if (!FormValidations.passwordRegex.hasMatch(value)) {
      return 'Enter a valid password';
    }
    return null;
  }

  static String? validateCurrentPassword(
      String? value, String currentPassword) {
    if (value == null || value.isEmpty) {
      return 'Current password is required';
    }
    if (!FormValidations.passwordRegex.hasMatch(value)) {
      return 'Enter a valid password';
    }
    if (value != currentPassword) {
      return 'Current password is incorrect';
    }
    return null;
  }

  static String? validateNewPassword(String? value) {
    if (value == null || value.isEmpty) {
      return null;
    }
    if (!FormValidations.passwordRegex.hasMatch(value)) {
      return 'Enter a valid password';
    }
    return null;
  }

  static String? validateConfirmNewPassword(String? value, String newPassword) {
    if (newPassword.isNotEmpty && value == null || value!.isEmpty) {
      return 'Confirm new password is required';
    }
    if (value.isNotEmpty && value != newPassword) {
      return 'Passwords do not match';
    }
    if (value.isNotEmpty && !FormValidations.passwordRegex.hasMatch(value)) {
      return 'Enter a valid password';
    }
    return null;
  }
}
