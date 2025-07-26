# HELP application_ready_time_seconds Time taken for the application to be ready to service requests
# TYPE application_ready_time_seconds gauge
application_ready_time_seconds{application="db-comparison",main_application_class="com.benchmarking.dbcomparison.DbComparisonApplication"} 8.502
# HELP application_started_time_seconds Time taken to start the application
# TYPE application_started_time_seconds gauge
application_started_time_seconds{application="db-comparison",main_application_class="com.benchmarking.dbcomparison.DbComparisonApplication"} 8.437
# HELP db_cache_hits_total
# TYPE db_cache_hits_total counter
db_cache_hits_total{application="db-comparison",database="postgres",operation="SELECT",result="hit"} 1000.0
# HELP db_connections_active
# TYPE db_connections_active gauge
db_connections_active{application="db-comparison",database="postgres"} 10.0
# HELP db_operation_time_seconds
# TYPE db_operation_time_seconds summary
db_operation_time_seconds_count{application="db-comparison",database="postgres",operation="DELETE"} 14
db_operation_time_seconds_sum{application="db-comparison",database="postgres",operation="DELETE"} 2.0279524
db_operation_time_seconds_count{application="db-comparison",database="postgres",operation="INSERT"} 26
db_operation_time_seconds_sum{application="db-comparison",database="postgres",operation="INSERT"} 2.401183
db_operation_time_seconds_count{application="db-comparison",database="postgres",operation="brand_category_insert"} 1
db_operation_time_seconds_sum{application="db-comparison",database="postgres",operation="brand_category_insert"} 0.224941
db_operation_time_seconds_count{application="db-comparison",database="postgres",operation="customer_insert"} 1
db_operation_time_seconds_sum{application="db-comparison",database="postgres",operation="customer_insert"} 1.0149912
db_operation_time_seconds_count{application="db-comparison",database="postgres",operation="inventory_movement_insert"} 1
db_operation_time_seconds_sum{application="db-comparison",database="postgres",operation="inventory_movement_insert"} 0.2578074
db_operation_time_seconds_count{application="db-comparison",database="postgres",operation="order_insert"} 1
db_operation_time_seconds_sum{application="db-comparison",database="postgres",operation="order_insert"} 0.5268246
db_operation_time_seconds_count{application="db-comparison",database="postgres",operation="product_insert"} 1
db_operation_time_seconds_sum{application="db-comparison",database="postgres",operation="product_insert"} 0.5431353
db_operation_time_seconds_count{application="db-comparison",database="postgres",operation="product_review_insert"} 1
db_operation_time_seconds_sum{application="db-comparison",database="postgres",operation="product_review_insert"} 0.2539738
# HELP db_operation_time_seconds_max
# TYPE db_operation_time_seconds_max gauge
db_operation_time_seconds_max{application="db-comparison",database="postgres",operation="DELETE"} 0.6004335
db_operation_time_seconds_max{application="db-comparison",database="postgres",operation="INSERT"} 0.8231838
db_operation_time_seconds_max{application="db-comparison",database="postgres",operation="brand_category_insert"} 0.224941
db_operation_time_seconds_max{application="db-comparison",database="postgres",operation="customer_insert"} 1.0149912
db_operation_time_seconds_max{application="db-comparison",database="postgres",operation="inventory_movement_insert"} 0.2578074
db_operation_time_seconds_max{application="db-comparison",database="postgres",operation="order_insert"} 0.5268246
db_operation_time_seconds_max{application="db-comparison",database="postgres",operation="product_insert"} 0.5431353
db_operation_time_seconds_max{application="db-comparison",database="postgres",operation="product_review_insert"} 0.2539738
# HELP db_operations_total
# TYPE db_operations_total counter
db_operations_total{application="db-comparison",database="postgres",operation="DELETE"} 14.0
db_operations_total{application="db-comparison",database="postgres",operation="INSERT"} 26.0
db_operations_total{application="db-comparison",database="postgres",operation="brand_insert"} 1.0
db_operations_total{application="db-comparison",database="postgres",operation="category_insert"} 1.0
db_operations_total{application="db-comparison",database="postgres",operation="customer_insert"} 1.0
db_operations_total{application="db-comparison",database="postgres",operation="inventory_movement_insert"} 1.0
db_operations_total{application="db-comparison",database="postgres",operation="order_insert"} 1.0
db_operations_total{application="db-comparison",database="postgres",operation="product_insert"} 1.0
db_operations_total{application="db-comparison",database="postgres",operation="product_review_insert"} 1.0
# HELP db_queries_failed_total Number of failed database queries
# TYPE db_queries_failed_total counter
db_queries_failed_total{application="db-comparison"} 0.0
# HELP db_table_size
# TYPE db_table_size gauge
db_table_size{application="db-comparison",database="postgres",table="$proxy145"} NaN
db_table_size{application="db-comparison",database="postgres",table="$proxy146"} 50.0
db_table_size{application="db-comparison",database="postgres",table="$proxy149"} NaN
db_table_size{application="db-comparison",database="postgres",table="$proxy150"} NaN
db_table_size{application="db-comparison",database="postgres",table="$proxy151"} NaN
db_table_size{application="db-comparison",database="postgres",table="$proxy152"} NaN
db_table_size{application="db-comparison",database="postgres",table="brands"} 50.0
db_table_size{application="db-comparison",database="postgres",table="categories"} 20.0
db_table_size{application="db-comparison",database="postgres",table="customers"} NaN
db_table_size{application="db-comparison",database="postgres",table="inventory_movements"} NaN
db_table_size{application="db-comparison",database="postgres",table="orders"} NaN
db_table_size{application="db-comparison",database="postgres",table="product_reviews"} NaN
db_table_size{application="db-comparison",database="postgres",table="products"} NaN
# HELP db_threads_active
# TYPE db_threads_active gauge
db_threads_active{application="db-comparison",database="postgres",operation="DELETE"} 22.0
db_threads_active{application="db-comparison",database="postgres",operation="INSERT"} 24.0
# HELP db_transaction_duration_seconds Database transaction duration
# TYPE db_transaction_duration_seconds summary
db_transaction_duration_seconds{application="db-comparison",quantile="0.5"} 0.0
db_transaction_duration_seconds{application="db-comparison",quantile="0.95"} 0.0
db_transaction_duration_seconds{application="db-comparison",quantile="0.99"} 0.0
db_transaction_duration_seconds_count{application="db-comparison"} 0
db_transaction_duration_seconds_sum{application="db-comparison"} 0.0
# HELP db_transaction_duration_seconds_max Database transaction duration
# TYPE db_transaction_duration_seconds_max gauge
db_transaction_duration_seconds_max{application="db-comparison"} 0.0
# HELP disk_free_bytes Usable space for path
# TYPE disk_free_bytes gauge
disk_free_bytes{application="db-comparison",path="C:\\Users\\Szymon\\Documents\\benchmark\\."} 6.6623389696E10
# HELP disk_total_bytes Total space for path
# TYPE disk_total_bytes gauge
disk_total_bytes{application="db-comparison",path="C:\\Users\\Szymon\\Documents\\benchmark\\."} 4.9887696896E11
# HELP executor_active_threads The approximate number of threads that are actively executing tasks
# TYPE executor_active_threads gauge
executor_active_threads{application="db-comparison",name="applicationTaskExecutor"} 0.0
# HELP executor_completed_tasks_total The approximate total number of tasks that have completed execution
# TYPE executor_completed_tasks_total counter
executor_completed_tasks_total{application="db-comparison",name="applicationTaskExecutor"} 0.0
# HELP executor_pool_core_threads The core number of threads for the pool
# TYPE executor_pool_core_threads gauge
executor_pool_core_threads{application="db-comparison",name="applicationTaskExecutor"} 8.0
# HELP executor_pool_max_threads The maximum allowed number of threads in the pool
# TYPE executor_pool_max_threads gauge
executor_pool_max_threads{application="db-comparison",name="applicationTaskExecutor"} 2.147483647E9
# HELP executor_pool_size_threads The current number of threads in the pool
# TYPE executor_pool_size_threads gauge
executor_pool_size_threads{application="db-comparison",name="applicationTaskExecutor"} 0.0
# HELP executor_queue_remaining_tasks The number of additional elements that this queue can ideally accept without blocking
# TYPE executor_queue_remaining_tasks gauge
executor_queue_remaining_tasks{application="db-comparison",name="applicationTaskExecutor"} 2.147483647E9
# HELP executor_queued_tasks The approximate number of tasks that are queued for execution
# TYPE executor_queued_tasks gauge
executor_queued_tasks{application="db-comparison",name="applicationTaskExecutor"} 0.0
# HELP hikaricp_connections Total connections
# TYPE hikaricp_connections gauge
hikaricp_connections{application="db-comparison",pool="HikariPool-1"} 10.0
# HELP hikaricp_connections_acquire_seconds Connection acquire time
# TYPE hikaricp_connections_acquire_seconds summary
hikaricp_connections_acquire_seconds_count{application="db-comparison",pool="HikariPool-1"} 1
hikaricp_connections_acquire_seconds_sum{application="db-comparison",pool="HikariPool-1"} 0.0204157
# HELP hikaricp_connections_acquire_seconds_max Connection acquire time
# TYPE hikaricp_connections_acquire_seconds_max gauge
hikaricp_connections_acquire_seconds_max{application="db-comparison",pool="HikariPool-1"} 0.0204157
# HELP hikaricp_connections_active Active connections
# TYPE hikaricp_connections_active gauge
hikaricp_connections_active{application="db-comparison",pool="HikariPool-1"} 0.0
# HELP hikaricp_connections_creation_seconds Connection creation time
# TYPE hikaricp_connections_creation_seconds summary
hikaricp_connections_creation_seconds_count{application="db-comparison",pool="HikariPool-1"} 10
hikaricp_connections_creation_seconds_sum{application="db-comparison",pool="HikariPool-1"} 0.156
# HELP hikaricp_connections_creation_seconds_max Connection creation time
# TYPE hikaricp_connections_creation_seconds_max gauge
hikaricp_connections_creation_seconds_max{application="db-comparison",pool="HikariPool-1"} 0.02
# HELP hikaricp_connections_idle Idle connections
# TYPE hikaricp_connections_idle gauge
hikaricp_connections_idle{application="db-comparison",pool="HikariPool-1"} 10.0
# HELP hikaricp_connections_max Max connections
# TYPE hikaricp_connections_max gauge
hikaricp_connections_max{application="db-comparison",pool="HikariPool-1"} 10.0
# HELP hikaricp_connections_min Min connections
# TYPE hikaricp_connections_min gauge
hikaricp_connections_min{application="db-comparison",pool="HikariPool-1"} 10.0
# HELP hikaricp_connections_pending Pending threads
# TYPE hikaricp_connections_pending gauge
hikaricp_connections_pending{application="db-comparison",pool="HikariPool-1"} 0.0
# HELP hikaricp_connections_timeout_total Connection timeout total count
# TYPE hikaricp_connections_timeout_total counter
hikaricp_connections_timeout_total{application="db-comparison",pool="HikariPool-1"} 0.0
# HELP hikaricp_connections_usage_seconds Connection usage time
# TYPE hikaricp_connections_usage_seconds summary
hikaricp_connections_usage_seconds_count{application="db-comparison",pool="HikariPool-1"} 1
hikaricp_connections_usage_seconds_sum{application="db-comparison",pool="HikariPool-1"} 4.836
# HELP hikaricp_connections_usage_seconds_max Connection usage time
# TYPE hikaricp_connections_usage_seconds_max gauge
hikaricp_connections_usage_seconds_max{application="db-comparison",pool="HikariPool-1"} 4.836
# HELP http_server_requests_active_seconds
# TYPE http_server_requests_active_seconds histogram
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="120.0"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="137.438953471"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="160.345445716"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="183.251937961"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="206.158430206"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="229.064922451"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="251.971414696"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="274.877906944"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="366.503875925"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="458.129844906"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="549.755813887"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="641.381782868"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="733.007751849"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="824.63372083"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="916.259689811"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="1007.885658792"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="1099.511627776"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="1466.015503701"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="1832.519379626"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="2199.023255551"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="2565.527131476"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="2932.031007401"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="3298.534883326"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="3665.038759251"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="4031.542635176"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="4398.046511104"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="5864.062014805"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="7200.0"} 1
http_server_requests_active_seconds_bucket{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN",le="+Inf"} 1
# HELP http_server_requests_active_seconds_gcount
# TYPE http_server_requests_active_seconds_gcount gauge
http_server_requests_active_seconds_gcount{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN"} 1
# HELP http_server_requests_active_seconds_gsum
# TYPE http_server_requests_active_seconds_gsum gauge
http_server_requests_active_seconds_gsum{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN"} 0.0240978
# HELP http_server_requests_active_seconds_max
# TYPE http_server_requests_active_seconds_max gauge
http_server_requests_active_seconds_max{application="db-comparison",exception="none",method="GET",outcome="SUCCESS",status="200",uri="UNKNOWN"} 0.0241023
# HELP http_server_requests_seconds
# TYPE http_server_requests_seconds histogram
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.001"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.001048576"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.001398101"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.001747626"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.002097151"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.002446676"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.002796201"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.003145726"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.003495251"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.003844776"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.004194304"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.005592405"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.006990506"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.008388607"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.009786708"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.011184809"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.01258291"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.013981011"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.015379112"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.016777216"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.022369621"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.027962026"} 19
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.033554431"} 47
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.039146836"} 55
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.044739241"} 58
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.050331646"} 59
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.055924051"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.061516456"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.067108864"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.089478485"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.111848106"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.134217727"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.156587348"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.178956969"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.20132659"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.223696211"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.246065832"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.268435456"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.357913941"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.447392426"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.536870911"} 60
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.626349396"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.715827881"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.805306366"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.894784851"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="0.984263336"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="1.073741824"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="1.431655765"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="1.789569706"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="2.147483647"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="2.505397588"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="2.863311529"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="3.22122547"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="3.579139411"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="3.937053352"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="4.294967296"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="5.726623061"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="7.158278826"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="8.589934591"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="10.021590356"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="11.453246121"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="12.884901886"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="14.316557651"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="15.748213416"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="17.179869184"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="22.906492245"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="28.633115306"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="30.0"} 61
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus",le="+Inf"} 61
http_server_requests_seconds_count{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus"} 61
http_server_requests_seconds_sum{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus"} 2.4151585
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.001"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.001048576"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.001398101"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.001747626"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.002097151"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.002446676"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.002796201"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.003145726"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.003495251"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.003844776"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.004194304"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.005592405"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.006990506"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.008388607"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.009786708"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.011184809"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.01258291"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.013981011"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.015379112"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.016777216"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.022369621"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.027962026"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.033554431"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.039146836"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.044739241"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.050331646"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.055924051"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.061516456"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.067108864"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.089478485"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.111848106"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.134217727"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.156587348"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.178956969"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.20132659"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.223696211"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.246065832"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.268435456"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.357913941"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.447392426"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.536870911"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.626349396"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.715827881"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.805306366"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.894784851"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="0.984263336"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="1.073741824"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="1.431655765"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="1.789569706"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="2.147483647"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="2.505397588"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="2.863311529"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="3.22122547"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="3.579139411"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="3.937053352"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="4.294967296"} 0
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="5.726623061"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="7.158278826"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="8.589934591"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="10.021590356"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="11.453246121"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="12.884901886"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="14.316557651"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="15.748213416"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="17.179869184"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="22.906492245"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="28.633115306"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="30.0"} 1
http_server_requests_seconds_bucket{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert",le="+Inf"} 1
http_server_requests_seconds_count{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert"} 1
http_server_requests_seconds_sum{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert"} 4.9067551
# HELP http_server_requests_seconds_max
# TYPE http_server_requests_seconds_max gauge
http_server_requests_seconds_max{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/actuator/prometheus"} 0.0504342
http_server_requests_seconds_max{application="db-comparison",error="none",exception="none",method="GET",outcome="SUCCESS",status="200",uri="/benchmark/insert"} 4.9067551
# HELP jdbc_connections_active Current number of active connections that have been allocated from the data source.
# TYPE jdbc_connections_active gauge
jdbc_connections_active{application="db-comparison",name="dataSource"} 0.0
# HELP jdbc_connections_idle Number of established but idle connections.
# TYPE jdbc_connections_idle gauge
jdbc_connections_idle{application="db-comparison",name="dataSource"} 10.0
# HELP jdbc_connections_max Maximum number of active connections that can be allocated at the same time.
# TYPE jdbc_connections_max gauge
jdbc_connections_max{application="db-comparison",name="dataSource"} 10.0
# HELP jdbc_connections_min Minimum number of idle connections in the pool.
# TYPE jdbc_connections_min gauge
jdbc_connections_min{application="db-comparison",name="dataSource"} 10.0
# HELP jvm_info JVM version info
# TYPE jvm_info gauge
jvm_info{application="db-comparison",runtime="OpenJDK Runtime Environment",vendor="Oracle Corporation",version="23.0.1+11-39"} 1
# HELP jvm_buffer_count_buffers An estimate of the number of buffers in the pool
# TYPE jvm_buffer_count_buffers gauge
jvm_buffer_count_buffers{application="db-comparison",id="direct"} 12.0
jvm_buffer_count_buffers{application="db-comparison",id="mapped"} 0.0
jvm_buffer_count_buffers{application="db-comparison",id="mapped - 'non-volatile memory'"} 0.0
# HELP jvm_buffer_memory_used_bytes An estimate of the memory that the Java virtual machine is using for this buffer pool
# TYPE jvm_buffer_memory_used_bytes gauge
jvm_buffer_memory_used_bytes{application="db-comparison",id="direct"} 98304.0
jvm_buffer_memory_used_bytes{application="db-comparison",id="mapped"} 0.0
jvm_buffer_memory_used_bytes{application="db-comparison",id="mapped - 'non-volatile memory'"} 0.0
# HELP jvm_buffer_total_capacity_bytes An estimate of the total capacity of the buffers in this pool
# TYPE jvm_buffer_total_capacity_bytes gauge
jvm_buffer_total_capacity_bytes{application="db-comparison",id="direct"} 98304.0
jvm_buffer_total_capacity_bytes{application="db-comparison",id="mapped"} 0.0
jvm_buffer_total_capacity_bytes{application="db-comparison",id="mapped - 'non-volatile memory'"} 0.0
# HELP jvm_classes_loaded_classes The number of classes that are currently loaded in the Java virtual machine
# TYPE jvm_classes_loaded_classes gauge
jvm_classes_loaded_classes{application="db-comparison"} 17858.0
# HELP jvm_classes_unloaded_classes_total The number of classes unloaded in the Java virtual machine
# TYPE jvm_classes_unloaded_classes_total counter
jvm_classes_unloaded_classes_total{application="db-comparison"} 0.0
# HELP jvm_compilation_time_ms_total The approximate accumulated elapsed time spent in compilation
# TYPE jvm_compilation_time_ms_total counter
jvm_compilation_time_ms_total{application="db-comparison",compiler="HotSpot 64-Bit Tiered Compilers"} 31403.0
# HELP jvm_gc_concurrent_phase_time_seconds Time spent in concurrent phase
# TYPE jvm_gc_concurrent_phase_time_seconds summary
jvm_gc_concurrent_phase_time_seconds_count{action="end of concurrent GC pause",application="db-comparison",cause="No GC",gc="G1 Concurrent GC"} 6
jvm_gc_concurrent_phase_time_seconds_sum{action="end of concurrent GC pause",application="db-comparison",cause="No GC",gc="G1 Concurrent GC"} 0.013
# HELP jvm_gc_concurrent_phase_time_seconds_max Time spent in concurrent phase
# TYPE jvm_gc_concurrent_phase_time_seconds_max gauge
jvm_gc_concurrent_phase_time_seconds_max{action="end of concurrent GC pause",application="db-comparison",cause="No GC",gc="G1 Concurrent GC"} 0.007
# HELP jvm_gc_live_data_size_bytes Size of long-lived heap memory pool after reclamation
# TYPE jvm_gc_live_data_size_bytes gauge
jvm_gc_live_data_size_bytes{application="db-comparison"} 4.5445536E7
# HELP jvm_gc_max_data_size_bytes Max size of long-lived heap memory pool
# TYPE jvm_gc_max_data_size_bytes gauge
jvm_gc_max_data_size_bytes{application="db-comparison"} 4.27819008E9
# HELP jvm_gc_memory_allocated_bytes_total Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next
# TYPE jvm_gc_memory_allocated_bytes_total counter
jvm_gc_memory_allocated_bytes_total{application="db-comparison"} 2.76824064E8
# HELP jvm_gc_memory_promoted_bytes_total Count of positive increases in the size of the old generation memory pool before GC to after GC
# TYPE jvm_gc_memory_promoted_bytes_total counter
jvm_gc_memory_promoted_bytes_total{application="db-comparison"} 2280600.0
# HELP jvm_gc_overhead An approximation of the percent of CPU time used by GC activities over the last lookback period or since monitoring began, whichever is shorter, in the range [0..1]
# TYPE jvm_gc_overhead gauge
jvm_gc_overhead{application="db-comparison"} 6.333333333333333E-5
# HELP jvm_gc_pause_seconds Time spent in GC pause
# TYPE jvm_gc_pause_seconds summary
jvm_gc_pause_seconds_count{action="end of minor GC",application="db-comparison",cause="G1 Evacuation Pause",gc="G1 Young Generation"} 8
jvm_gc_pause_seconds_sum{action="end of minor GC",application="db-comparison",cause="G1 Evacuation Pause",gc="G1 Young Generation"} 0.022
# HELP jvm_gc_pause_seconds_max Time spent in GC pause
# TYPE jvm_gc_pause_seconds_max gauge
jvm_gc_pause_seconds_max{action="end of minor GC",application="db-comparison",cause="G1 Evacuation Pause",gc="G1 Young Generation"} 0.002
# HELP jvm_memory_committed_bytes The amount of memory in bytes that is committed for the Java virtual machine to use
# TYPE jvm_memory_committed_bytes gauge
jvm_memory_committed_bytes{application="db-comparison",area="heap",id="G1 Eden Space"} 3.5651584E7
jvm_memory_committed_bytes{application="db-comparison",area="heap",id="G1 Old Gen"} 5.8720256E7
jvm_memory_committed_bytes{application="db-comparison",area="heap",id="G1 Survivor Space"} 4194304.0
jvm_memory_committed_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'non-nmethods'"} 2555904.0
jvm_memory_committed_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'non-profiled nmethods'"} 6225920.0
jvm_memory_committed_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'profiled nmethods'"} 1.7104896E7
jvm_memory_committed_bytes{application="db-comparison",area="nonheap",id="Compressed Class Space"} 1.2845056E7
jvm_memory_committed_bytes{application="db-comparison",area="nonheap",id="Metaspace"} 9.0898432E7
# HELP jvm_memory_max_bytes The maximum amount of memory in bytes that can be used for memory management
# TYPE jvm_memory_max_bytes gauge
jvm_memory_max_bytes{application="db-comparison",area="heap",id="G1 Eden Space"} -1.0
jvm_memory_max_bytes{application="db-comparison",area="heap",id="G1 Old Gen"} 4.27819008E9
jvm_memory_max_bytes{application="db-comparison",area="heap",id="G1 Survivor Space"} -1.0
jvm_memory_max_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'non-nmethods'"} 5832704.0
jvm_memory_max_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'non-profiled nmethods'"} 1.22945536E8
jvm_memory_max_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'profiled nmethods'"} 1.2288E8
jvm_memory_max_bytes{application="db-comparison",area="nonheap",id="Compressed Class Space"} 1.073741824E9
jvm_memory_max_bytes{application="db-comparison",area="nonheap",id="Metaspace"} -1.0
# HELP jvm_memory_usage_after_gc The percentage of long-lived heap pool used after the last GC event, in the range [0..1]
# TYPE jvm_memory_usage_after_gc gauge
jvm_memory_usage_after_gc{application="db-comparison",area="heap",pool="long-lived"} 0.010786290262259688
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{application="db-comparison",area="heap",id="G1 Eden Space"} 1.2582912E7
jvm_memory_used_bytes{application="db-comparison",area="heap",id="G1 Old Gen"} 4.61458E7
jvm_memory_used_bytes{application="db-comparison",area="heap",id="G1 Survivor Space"} 2394880.0
jvm_memory_used_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'non-nmethods'"} 1810176.0
jvm_memory_used_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'non-profiled nmethods'"} 5820672.0
jvm_memory_used_bytes{application="db-comparison",area="nonheap",id="CodeHeap 'profiled nmethods'"} 1.4352512E7
jvm_memory_used_bytes{application="db-comparison",area="nonheap",id="Compressed Class Space"} 1.2377384E7
jvm_memory_used_bytes{application="db-comparison",area="nonheap",id="Metaspace"} 8.9872416E7
# HELP jvm_threads_daemon_threads The current number of live daemon threads
# TYPE jvm_threads_daemon_threads gauge
jvm_threads_daemon_threads{application="db-comparison"} 24.0
# HELP jvm_threads_live_threads The current number of live threads including both daemon and non-daemon threads
# TYPE jvm_threads_live_threads gauge
jvm_threads_live_threads{application="db-comparison"} 28.0
# HELP jvm_threads_peak_threads The peak live thread count since the Java virtual machine started or peak was reset
# TYPE jvm_threads_peak_threads gauge
jvm_threads_peak_threads{application="db-comparison"} 31.0
# HELP jvm_threads_started_threads_total The total number of application threads started in the JVM
# TYPE jvm_threads_started_threads_total counter
jvm_threads_started_threads_total{application="db-comparison"} 37.0
# HELP jvm_threads_states_threads The current number of threads
# TYPE jvm_threads_states_threads gauge
jvm_threads_states_threads{application="db-comparison",state="blocked"} 0.0
jvm_threads_states_threads{application="db-comparison",state="new"} 0.0
jvm_threads_states_threads{application="db-comparison",state="runnable"} 10.0
jvm_threads_states_threads{application="db-comparison",state="terminated"} 0.0
jvm_threads_states_threads{application="db-comparison",state="timed-waiting"} 7.0
jvm_threads_states_threads{application="db-comparison",state="waiting"} 11.0
# HELP logback_events_total Number of log events that were enabled by the effective log level
# TYPE logback_events_total counter
logback_events_total{application="db-comparison",level="debug"} 0.0
logback_events_total{application="db-comparison",level="error"} 0.0
logback_events_total{application="db-comparison",level="info"} 13.0
logback_events_total{application="db-comparison",level="trace"} 0.0
logback_events_total{application="db-comparison",level="warn"} 11.0
# HELP process_cpu_time_ns_total The "cpu time" used by the Java Virtual Machine process
# TYPE process_cpu_time_ns_total counter
process_cpu_time_ns_total{application="db-comparison"} 4.846875E10
# HELP process_cpu_usage The "recent cpu usage" for the Java Virtual Machine process
# TYPE process_cpu_usage gauge
process_cpu_usage{application="db-comparison"} 0.016003636904218434
# HELP process_start_time_seconds Start time of the process since unix epoch.
# TYPE process_start_time_seconds gauge
process_start_time_seconds{application="db-comparison"} 1.753556103707E9
# HELP process_uptime_seconds The uptime of the Java virtual machine
# TYPE process_uptime_seconds gauge
process_uptime_seconds{application="db-comparison"} 352.063
# HELP spring_data_repository_invocations_seconds Duration of repository invocations
# TYPE spring_data_repository_invocations_seconds summary
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="deleteAll",repository="BrandRepository",state="SUCCESS"} 2
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="deleteAll",repository="BrandRepository",state="SUCCESS"} 0.0076749
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="deleteAll",repository="CustomerRepository",state="SUCCESS"} 2
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="deleteAll",repository="CustomerRepository",state="SUCCESS"} 0.0163379
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="deleteAll",repository="InventoryMovementRepository",state="SUCCESS"} 2
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="deleteAll",repository="InventoryMovementRepository",state="SUCCESS"} 0.1103972
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="deleteAll",repository="OrderRepository",state="SUCCESS"} 2
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="deleteAll",repository="OrderRepository",state="SUCCESS"} 0.0258313
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="deleteAll",repository="ProductCategoryRepository",state="SUCCESS"} 2
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="deleteAll",repository="ProductCategoryRepository",state="SUCCESS"} 0.0068301
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="deleteAll",repository="ProductRepository",state="SUCCESS"} 2
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="deleteAll",repository="ProductRepository",state="SUCCESS"} 0.0166671
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="deleteAll",repository="ProductReviewRepository",state="SUCCESS"} 2
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="deleteAll",repository="ProductReviewRepository",state="SUCCESS"} 0.0249775
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="save",repository="ProductCategoryRepository",state="SUCCESS"} 20
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="save",repository="ProductCategoryRepository",state="SUCCESS"} 0.0033316
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="saveAll",repository="BrandRepository",state="SUCCESS"} 1
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="saveAll",repository="BrandRepository",state="SUCCESS"} 0.0022035
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="saveAll",repository="CustomerRepository",state="SUCCESS"} 1
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="saveAll",repository="CustomerRepository",state="SUCCESS"} 0.0781002
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="saveAll",repository="InventoryMovementRepository",state="SUCCESS"} 1
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="saveAll",repository="InventoryMovementRepository",state="SUCCESS"} 0.0079034
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="saveAll",repository="OrderRepository",state="SUCCESS"} 1
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="saveAll",repository="OrderRepository",state="SUCCESS"} 0.029797
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="saveAll",repository="ProductRepository",state="SUCCESS"} 1
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="saveAll",repository="ProductRepository",state="SUCCESS"} 0.0125673
spring_data_repository_invocations_seconds_count{application="db-comparison",exception="None",method="saveAll",repository="ProductReviewRepository",state="SUCCESS"} 1
spring_data_repository_invocations_seconds_sum{application="db-comparison",exception="None",method="saveAll",repository="ProductReviewRepository",state="SUCCESS"} 0.0087982
# HELP spring_data_repository_invocations_seconds_max Duration of repository invocations
# TYPE spring_data_repository_invocations_seconds_max gauge
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="deleteAll",repository="BrandRepository",state="SUCCESS"} 0.0048081
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="deleteAll",repository="CustomerRepository",state="SUCCESS"} 0.0130215
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="deleteAll",repository="InventoryMovementRepository",state="SUCCESS"} 0.0757963
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="deleteAll",repository="OrderRepository",state="SUCCESS"} 0.0203712
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="deleteAll",repository="ProductCategoryRepository",state="SUCCESS"} 0.0040467
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="deleteAll",repository="ProductRepository",state="SUCCESS"} 0.0126251
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="deleteAll",repository="ProductReviewRepository",state="SUCCESS"} 0.0211608
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="save",repository="ProductCategoryRepository",state="SUCCESS"} 8.422E-4
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="saveAll",repository="BrandRepository",state="SUCCESS"} 0.0022035
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="saveAll",repository="CustomerRepository",state="SUCCESS"} 0.0781002
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="saveAll",repository="InventoryMovementRepository",state="SUCCESS"} 0.0079034
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="saveAll",repository="OrderRepository",state="SUCCESS"} 0.029797
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="saveAll",repository="ProductRepository",state="SUCCESS"} 0.0125673
spring_data_repository_invocations_seconds_max{application="db-comparison",exception="None",method="saveAll",repository="ProductReviewRepository",state="SUCCESS"} 0.0087982
# HELP system_cpu_count The number of processors available to the Java virtual machine
# TYPE system_cpu_count gauge
system_cpu_count{application="db-comparison"} 12.0
# HELP system_cpu_usage The "recent cpu usage" of the system the application is running in
# TYPE system_cpu_usage gauge
system_cpu_usage{application="db-comparison"} 0.1627920446731682
# HELP tomcat_sessions_active_current_sessions
# TYPE tomcat_sessions_active_current_sessions gauge
tomcat_sessions_active_current_sessions{application="db-comparison"} 0.0
# HELP tomcat_sessions_active_max_sessions
# TYPE tomcat_sessions_active_max_sessions gauge
tomcat_sessions_active_max_sessions{application="db-comparison"} 0.0
# HELP tomcat_sessions_alive_max_seconds
# TYPE tomcat_sessions_alive_max_seconds gauge
tomcat_sessions_alive_max_seconds{application="db-comparison"} 0.0
# HELP tomcat_sessions_created_sessions_total
# TYPE tomcat_sessions_created_sessions_total counter
tomcat_sessions_created_sessions_total{application="db-comparison"} 0.0
# HELP tomcat_sessions_expired_sessions_total
# TYPE tomcat_sessions_expired_sessions_total counter
tomcat_sessions_expired_sessions_total{application="db-comparison"} 0.0
# HELP tomcat_sessions_rejected_sessions_total
# TYPE tomcat_sessions_rejected_sessions_total counter
tomcat_sessions_rejected_sessions_total{application="db-comparison"} 0.0