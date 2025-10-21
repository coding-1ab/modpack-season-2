Veil provides an expansive Color class, with a basic Filter system for said colors. The Color class itself contains a
lot of utility methods, including methods to saturate, desaturate, lighten, darken, invert and mix colors. Colors can be
created from RGB(A) float values, RGB(A) int values and RGBA hex values (including strings), and can be processed back
into one of those, i.e you can go from an RGBA float value, to a Veil Color, to a hex string.

Veil also provides some default colors, along with some useful vanilla Minecraft colors - such as the 3 vanilla tooltip
color values.

## Filters

Veil provides some basic Color filters by default, Sepia and Grayscale. By
calling `FILTER.apply(Color color, float value);`, you will apply the filter to that color.

`IFilterType` is a functional interface, allowing extensibility by other developers in their projects.

# Themes

Color Themes are an expansive, dynamic way for developers to have customizable color configurations for their mods.
Color themes are quite abstract, in the way that they do not have a singular defined use. They can be used for Veil's
Tooltippable system, they could be used for GUIs, customization, etc.

A Color Theme is made up of 2 maps, a String optional, to either a color or an `IThemeProperties`. Colors are
self-explanatory, and use Veil's provided Colors class.

## IThemeProperties

IThemeProperty is an interface that declares a class as being able to be used by the Color Theme system. It effectively
allows you to store arbitrary code to a theme. Veil by default supplies a Boolean, Number, String and Consumer theme
property class. When stored, these would look something like:

```java
Optional<String>,Boolean
Optional<String>,Number
Optional<String>,String
Optional<String>,Consumer<?>
```

This allows you almost endless uses for Color Themes.
If you are requesting a property from a theme and you know the type of the property you're requesting, you will want to
call `getAndCastProperty`, for example:

```java
ConsumerThemeProperty consumerProp = (ConsumerThemeProperty) theme.getAndCastProperty("consumerProp");
```
