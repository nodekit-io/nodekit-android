package io.nodekit.engine.javascriptcore;

/**
 * A JSContextGroup associates JavaScript contexts with one another. Contexts
 * in the same group may share and exchange JavaScript objects. Sharing and/or
 * exchanging JavaScript objects between contexts in different groups will produce
 * undefined behavior. When objects from the same context group are used in multiple
 * threads, explicit synchronization is required.
 */
public class JSContextGroup {

    private Long group;

    /**
     * Creates a new context group
     *
     *
     */
    public JSContextGroup() {
        group = NJScreate();
    }

    /**
     * Wraps an existing context group
     *
     * @param groupRef the JavaScriptCore context group reference
     *
     */
    public JSContextGroup(Long groupRef) {
        group = groupRef;
    }

    @Override
    protected void finalize() throws Throwable {
        if (group != 0) NJSrelease(group);
        super.finalize();
    }

    /**
     * Gets the JavaScriptCore context group reference
     *
     * @return the JavaScriptCore context group reference
     *
     */
    public Long groupRef() {
        return group;
    }

    /**
     * Checks if two JSContextGroups refer to the same JS context group
     *
     * @param other the other object to compare
     * @return true if refer to same context group, false otherwise
     *
     */
    @Override
    public boolean equals(Object other) {
        return (other != null) &&
                (this == other) ||
                (other instanceof JSContextGroup) &&
                        !(groupRef() == null || groupRef() == 0) &&
                        groupRef().equals(((JSContextGroup) other).groupRef());
    }

    protected native long NJScreate();

    protected native long NJSretain(long group);

    protected native void NJSrelease(long group);
}
