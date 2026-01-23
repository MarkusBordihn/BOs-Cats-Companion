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
import java.util.UUID;
import javax.annotation.Nullable;

public class CatOwnerComponent implements Component<EntityStore>, Serializable {
  @Nullable private UUID ownerId;
  @Nullable private String ownerName;
  @Nullable private String catName;
  private long tamedTimestamp;

  public CatOwnerComponent() {
    this.ownerId = null;
    this.ownerName = null;
    this.catName = null;
    this.tamedTimestamp = 0;
  }

  public CatOwnerComponent(UUID ownerId, String ownerName) {
    this.ownerId = ownerId;
    this.ownerName = ownerName;
    this.catName = null;
    this.tamedTimestamp = System.currentTimeMillis();
  }

  public CatOwnerComponent(UUID ownerId, String ownerName, String catName) {
    this.ownerId = ownerId;
    this.ownerName = ownerName;
    this.catName = catName;
    this.tamedTimestamp = System.currentTimeMillis();
  }

  public boolean hasOwner() {
    return ownerId != null;
  }

  @Nullable
  public UUID getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(@Nullable UUID ownerId) {
    this.ownerId = ownerId;
  }

  @Nullable
  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(@Nullable String ownerName) {
    this.ownerName = ownerName;
  }

  public long getTamedTimestamp() {
    return tamedTimestamp;
  }

  @Nullable
  public String getCatName() {
    return catName;
  }

  public void setCatName(@Nullable String catName) {
    this.catName = catName;
  }

  public void setOwner(UUID ownerId, String ownerName) {
    this.ownerId = ownerId;
    this.ownerName = ownerName;
    this.tamedTimestamp = System.currentTimeMillis();
  }

  public void setOwner(UUID ownerId, String ownerName, String catName) {
    this.ownerId = ownerId;
    this.ownerName = ownerName;
    this.catName = catName;
    this.tamedTimestamp = System.currentTimeMillis();
  }

  public void clearOwner() {
    this.ownerId = null;
    this.ownerName = null;
    this.catName = null;
    this.tamedTimestamp = 0;
  }

  @Override
  public CatOwnerComponent clone() {
    try {
      return (CatOwnerComponent) super.clone();
    } catch (CloneNotSupportedException e) {
      CatOwnerComponent copy = new CatOwnerComponent();
      copy.ownerId = this.ownerId;
      copy.ownerName = this.ownerName;
      copy.catName = this.catName;
      copy.tamedTimestamp = this.tamedTimestamp;
      return copy;
    }
  }
}
