/*******************************************************************************
 * Copyright (c) 2009 VMware, Inc. licensed under the terms of the BSD. All
 * other rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - Neither the name of VMware, Inc. nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.vix;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByReference;

/**
 * Child class of ByReference, which is an implementation of pointer to type
 * functionality. VixHandle values can now be returned by VIX functions instead
 * of using IntByReference and creating a VixHandle from that value.
 * <p/>
 * Use by creating an empty (or non-empty) VixHandleByReference and pass it
 * to a VIX function that modifies a VixHandle. Get the VixHandle result by
 * calling getValue().
 */
public class VixHandleByReference extends ByReference {

   private final VixHandle mVixHandle = new VixHandle();

   /**
    * Constructor.
    */
   public VixHandleByReference() {
      super(Pointer.SIZE);
   }

   /**
    * Constructor.
    *
    * @param v
    *           VixHandle that has already been set to something.
    */
   public VixHandleByReference(VixHandle v) {
      this();
      setValue(v.intValue());
   }

   /**
    * Sets the value of this VixHandle.
    *
    * @param value
    */
   public void setValue(int value) {
      getPointer().setInt(0, value);
      mVixHandle.setValue(value);
   }

   /**
    * Get a new VixHandle object from the underlying integer value.
    *
    * @return VixHandle
    */
   public VixHandle getValue() {
      mVixHandle.setValue(getPointer().getInt(0));
      return mVixHandle;
   }
}