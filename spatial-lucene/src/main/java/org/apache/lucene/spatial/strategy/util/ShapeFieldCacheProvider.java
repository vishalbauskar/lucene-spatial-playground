/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.spatial.strategy.util;

import java.io.IOException;
import java.util.WeakHashMap;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.spatial.base.shape.Shape;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ShapeFieldCacheProvider<T extends Shape> {
  static final Logger log = LoggerFactory.getLogger(ShapeFieldCacheProvider.class);

  // it may be a List<T> or T
  WeakHashMap<IndexReader, ShapeFieldCache<T>> sidx = new WeakHashMap<IndexReader, ShapeFieldCache<T>>();

  protected final int defaultSize;
  protected final String shapeField;

  public ShapeFieldCacheProvider(String shapeField, int defaultSize) {
    this.shapeField = shapeField;
    this.defaultSize = defaultSize;
  }

  protected abstract T readShape( BytesRef term );

  public synchronized ShapeFieldCache<T> getCache(IndexReader reader) throws CorruptIndexException, IOException {
    ShapeFieldCache<T> idx = sidx.get(reader);
    if (idx != null) {
      return idx;
    }
    long startTime = System.currentTimeMillis();

    log.info("Building Cache [" + reader.maxDoc() + "]");
    idx = new ShapeFieldCache<T>(reader.maxDoc(),defaultSize);
    int count = 0;
    DocsEnum docs = null;
    Terms terms = reader.terms(shapeField);
    if (terms != null) {
      TermsEnum te = terms.iterator();
      BytesRef term = te.next();
      while (term != null) {
        T shape = readShape(term);
        if( shape != null ) {
          docs = te.docs(null, docs);
          Integer docid = docs.nextDoc();
          while (docid != DocIdSetIterator.NO_MORE_DOCS) {
            idx.add( docid, shape );
            docid = docs.nextDoc();
            count++;
          }
        }
        term = te.next();
      }
    }
    sidx.put(reader, idx);
    long elapsed = System.currentTimeMillis() - startTime;
    log.info("Cached: [" + count + " in " + elapsed + "ms] " + idx);
    return idx;
  }
}
