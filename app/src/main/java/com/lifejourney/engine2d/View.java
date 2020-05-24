package com.lifejourney.engine2d;

public interface View {

    /**
     *
     */
    void close();

    /**
     *
     */
    void update();

    /**
     *
     */
    void commit();

    /**
     *
     */
    void show();

    /**
     *
     */
    void hide();

    /**
     *
     * @return
     */
    Size getSize();
}
