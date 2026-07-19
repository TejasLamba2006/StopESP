package com.github.tejaslamba2006.stopesp.module;

public interface Module {
   String getName();

   void onEnable();

   void onDisable();

   boolean isEnabled();
}
