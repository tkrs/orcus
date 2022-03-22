package orcus.example

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.ServiceOptions
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings
import com.google.cloud.bigtable.data.v2.BigtableDataSettings

object Settings {
  val adminSettings =
    if (sys.env.contains("BIGTABLE_EMULATOR_HOST"))
      BigtableTableAdminSettings.newBuilder().setProjectId("fake").setInstanceId("fake").build()
    else
      BigtableTableAdminSettings
        .newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault))
        .setProjectId(ServiceOptions.getDefaultProjectId)
        .setInstanceId(sys.env.getOrElse("BIGTABLE_INSTANCE", "fake"))
        .build()

  val dataSettings =
    if (sys.env.contains("BIGTABLE_EMULATOR_HOST"))
      BigtableDataSettings.newBuilder().setProjectId("fake").setInstanceId("fake").build()
    else
      BigtableDataSettings
        .newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.getApplicationDefault))
        .setProjectId(ServiceOptions.getDefaultProjectId)
        .setInstanceId(sys.env.getOrElse("BIGTABLE_INSTANCE", "fake"))
        .setAppProfileId(sys.env.getOrElse("BIGTABLE_APP_PROFILE", "default"))
        .build()
}
