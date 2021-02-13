package botUtilities.commandsSystem.types.function;

import botUtilities.tools.Num;

public class Value {
    private final Argument argument;
    private final String value;

    public Value(Argument argument, String value) {
        this.argument = argument;
        this.value = value;
    }

    public String getName() {
        return argument.getName();
    }

    public boolean matches(String key) {
        return argument.matches(key);
    }

    public String getValueString() {
        return value;
    }

    public int getValueInt() {
        // Have to parse it to a double and then cast to int otherwise some string formats won't work cause
        // parseInt is stupid
        return Num.sigFigs((int) Double.parseDouble(value), argument.getSigFigs());
    }

    public double getValueDouble() {
        return Num.sigFigs(Double.parseDouble(value), argument.getSigFigs());
    }

    public boolean getValueBoolean() {
        return Boolean.parseBoolean(value);
    }

    /**
     * Check to see if this Value object works. In other words, check that the `value` instance variable is compatible
     * with the Argument. If it's not compatible, throw an error. Otherwise do nothing.
     *
     * @throws IllegalArgumentException if the validation fails
     */
    public void validate() {
        String exceptionMsg = "Failed to parse **" + argument.getName() + "**. ";

        switch (argument.getType()) {
            case ARGUMENT_INTEGER -> {
                int v;
                try {
                    // Parse it to a double first and then type cast because otherwise `1e4` notation doesn't work
                    double d = Double.parseDouble(value);
                    v = (int) d;
                    assert d == v;
                } catch (Exception ignore) {
                    throw new IllegalArgumentException(exceptionMsg + "Use a valid integer.");
                }
                checkBounds(exceptionMsg, v);
            }

            case ARGUMENT_DOUBLE -> {
                double v;
                try {
                    v = Double.parseDouble(value);
                } catch (Exception ignore) {
                    System.out.println("Failed to parse " + value + " into double.");
                    throw new IllegalArgumentException(exceptionMsg + "Use a valid number.");
                }
                checkBounds(exceptionMsg, v);
            }

            case ARGUMENT_STRING -> {
                // If the string can be anything just return. It's validated
                if (argument.getAllowedValues() == null)
                    return;
                // Otherwise make sure the user selected one of the legal values
                for (String opt : argument.getAllowedValues())
                    if (value.equals(opt))
                        return;
                throw new IllegalArgumentException(
                        exceptionMsg + "Must be one of " + argument.getAllowedValuesStr() + ".");
            }
        }
    }

    /**
     * Checks to see if a given number is in an acceptable range, and if not an error is thrown
     *
     * @param exceptionMsg the first part of the possible exception, placed before "integer must be greater/less than"
     * @param v            the number to test
     * @throws IllegalArgumentException if the given number is not in the required range
     */
    public void checkBounds(String exceptionMsg, double v) {
        if (v < argument.getFloor() || (!argument.isFloorInclusive() && v == argument.getFloor()))
            throw new IllegalArgumentException(exceptionMsg + "Integer must be greater than " +
                    (argument.isFloorInclusive() ? "or equal to " : "") + argument.getFloor());
        if (v > argument.getCeiling() || (!argument.isCeilingInclusive() && v == argument.getCeiling()))
            throw new IllegalArgumentException(exceptionMsg + "Integer must be less than " +
                    (argument.isCeilingInclusive() ? "or equal to " : "") + argument.getCeiling());
    }
}
