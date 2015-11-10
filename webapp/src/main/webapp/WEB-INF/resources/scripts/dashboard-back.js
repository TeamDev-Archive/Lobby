angular.module("dashboard-back", ["ngResource"])
    .factory("DashboardBack", function ($resource) {
        return $resource("/dashboard/:path", {}, {
            test: {
                method: "GET",
                params: {
                    path: "test"
                }
            }
        });
    });