import 'package:flutter/material.dart';
import 'dart:ui';

class GlassmorphismWidget extends StatelessWidget {
  final Widget child;
  final double blur;
  final double opacity;
  final EdgeInsets margin;

  const GlassmorphismWidget({
    super.key,
    required this.child,
    this.blur = 10.0,
    this.opacity = 0.2,
    this.margin = const EdgeInsets.symmetric(horizontal: 20)
  });

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(20.0),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: blur, sigmaY: blur),
        child: Container(
          margin: margin,
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(opacity),
            borderRadius: BorderRadius.circular(20.0),
            border: Border.all(
              color: Colors.white.withOpacity(opacity),
            ),
          ),
          child: child,
        ),
      ),
    );
  }
}