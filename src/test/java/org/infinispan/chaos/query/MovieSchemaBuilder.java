package org.infinispan.chaos.query;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(schemaPackageName = "movie", includeClasses = Movie.class)
public interface MovieSchemaBuilder extends GeneratedSchema {
}