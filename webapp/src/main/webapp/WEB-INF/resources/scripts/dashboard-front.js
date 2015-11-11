angular.module("dashboard-front", ["ngRoute", "dashboard-back"])
    .config(["$routeProvider", function ($routeProvider) {

        $routeProvider
            .when("/dashboard", {
                controller: "dashboard-controller",
                templateUrl: "../pages/dashboard.html"
            })
            .otherwise({
                redirectTo: "/dashboard"
            });
    }])
    .controller("dashboard-controller", ["$scope", "DashboardBack", function ($scope, DashboardBack) {

        console.log("dashboard-controller");

        DashboardBack.test({}, function (response) {
            console.log("response:", response.result);
        });
    }]);