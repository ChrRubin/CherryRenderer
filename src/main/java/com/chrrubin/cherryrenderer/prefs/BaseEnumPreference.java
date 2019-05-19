package com.chrrubin.cherryrenderer.prefs;

public abstract class BaseEnumPreference<E extends Enum<E>> extends AbstractPreference<E> {
    BaseEnumPreference(String KEY, E DEFAULT){
        super(KEY, DEFAULT);
    }

    @Override
    public E get() {
        try {
            return E.valueOf(getDefault().getDeclaringClass(), getPreferencesNode().get(getKey(), getDefault().name()));
        }
        catch (IllegalArgumentException e){
            return getDefault();
        }
    }

    @Override
    public void put(E value) {
        getPreferencesNode().put(getKey(), value.name());
    }

    @Override
    public void reset() {
        getPreferencesNode().put(getKey(), getDefault().name());
    }
}
