/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elasticsearch.hadoop.cascading;

import java.util.List;

import org.apache.hadoop.io.Writable;
import org.elasticsearch.hadoop.mr.WritableValueWriter;
import org.elasticsearch.hadoop.serialization.Generator;
import org.elasticsearch.hadoop.serialization.JdkValueWriter;
import org.elasticsearch.hadoop.serialization.ValueWriter;

import cascading.scheme.SinkCall;
import cascading.tuple.Tuple;

public class CascadingValueWriter implements ValueWriter<SinkCall> {

    private boolean writeUnknownTypes;
    private final ValueWriter<Writable> writableWriter;
    private final ValueWriter<Object> jdkWriter;

    public CascadingValueWriter() {
        this(false);
    }

    public CascadingValueWriter(boolean writeUnknownTypes) {
        this.writeUnknownTypes = writeUnknownTypes;
        writableWriter = new WritableValueWriter(writeUnknownTypes);
        jdkWriter = new JdkValueWriter(writeUnknownTypes);
    }

    @Override
    public boolean write(SinkCall sinkCall, Generator generator) {
        Tuple tuple = sinkCall.getOutgoingEntry().getTuple();
        List<String> names = (List<String>) ((Object[]) sinkCall.getContext())[0];

        generator.writeBeginObject();
        for (int i = 0; i < tuple.size(); i++) {
            String name = (i < names.size() ? names.get(i) : "tuple" + i);
            generator.writeFieldName(name);
            if (!jdkWriter.write(tuple.getObject(i), generator)) {
                return false;
            }
        }
        generator.writeEndObject();
        return true;
    }
}
