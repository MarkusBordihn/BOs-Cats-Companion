/*
 * Copyright 2026 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.cats.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.io.Serializable;

public class CatStateComponent implements Component<EntityStore>, Serializable {
  private CatState state;

  public CatStateComponent() {
    this.state = CatState.WANDERING;
  }

  public CatStateComponent(CatState state) {
    this.state = state;
  }

  public CatState getState() {
    return state;
  }

  public void setState(CatState state) {
    this.state = state;
  }

  @Override
  public CatStateComponent clone() {
    try {
      return (CatStateComponent) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Clone not supported", e);
    }
  }

  public enum CatState {
    SITTING,
    SLEEPING,
    FOLLOWING,
    WANDERING,
    PLAYING,
    SEARCHING,
    WAITING
  }
}
