package com.lifejourney.engine2d;

public interface View {

    public void update();

    public void commit();

    public void show();

    public void hide();

    public Size getSize();
}
