import 'package:flutter/material.dart';

const Color primaryBlue = Color(0xFF0066CC);
const Color primaryGreen = Color(0xFF00BF7F);

const BoxDecoration gradientBackground = BoxDecoration(
  gradient: LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [
      primaryBlue,
      primaryGreen,
    ],
  ),
);