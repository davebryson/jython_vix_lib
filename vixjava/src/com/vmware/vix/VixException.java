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

/**
 * Exception for errors encountered while interacting with the VIX library.
 */
@SuppressWarnings("serial")
public class VixException extends Exception {

   private VixError mVixError;

   /**
    * Constructor.
    *
    * @param message
    */
   public VixException(String message) {
      super(message);
   }

   /**
    * Constructor.
    *
    * @param message
    * @param cause
    */
   public VixException(String message, Throwable cause) {
      super(message, cause);
   }

   /**
    * Constructor.
    *
    * @param cause
    */
   public VixException(Throwable cause) {
      super(cause);
   }

   /**
    * Default Constructor.
    */
   public VixException() {
      super();
   }

   /**
    * Constructor that will just take a VixError object. Calls to getMessage()
    * will provide the error message associated with the VixError.
    *
    * @param vixError
    */
   public VixException(VixError vixError) {
      this();
      this.mVixError = vixError;
   }

   /**
    * Get the specific error that was set, if any.
    *
    * @return VixError if set; null otherwise.
    */
   public VixError getError() {
      return mVixError;
   }

   /**
    * Overrides so that if a VixError has been set, the message will be set to
    * that error's specific message.
    */
   @Override
   public String getMessage() {
      /*
       * Check if a VixError has been set.
       */
      if (mVixError != null) {
         return VixLibrary.INSTANCE.Vix_GetErrorText(getError(), null);
      } else {
         return super.getMessage();
      }
   }
}