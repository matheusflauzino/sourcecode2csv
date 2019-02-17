/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.abdera.g14n.io;

import org.apache.abdera.g14n.io.CodepointIterator;

/**
 * Iterate over Unicode codepoints in a CharSequence (e.g. String, StringBuffer, etc)
 */
public class CharSequenceCodepointIterator 
  extends CodepointIterator {

  private CharSequence buffer;
  
  public CharSequenceCodepointIterator(CharSequence buffer) {
    this(buffer,0,buffer.length());
  }
  
  public CharSequenceCodepointIterator(CharSequence buffer, int n, int e) {
    this.buffer = buffer;
    this.position = n;
    this.limit = Math.min(buffer.length()-n,e);
  }
  
  protected char get() {
    return buffer.charAt(position++);
  }

  protected char get(int index) {
    return buffer.charAt(index);
  }
  
}
