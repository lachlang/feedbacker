/*
 * Controller for password reset
 */
fbControllers.controller('ActivationCtrl',  ['$scope', '$log', '$location', 'Account', function($scope, $log, $location, Account) {

    var ctrl = this;

    ctrl.error = undefined;
    ctrl.message = undefined;

	ctrl.sendActivationEmail = function(email) {
        ctrl.error = undefined;
        ctrl.message = undefined;
        Account.sendActivationEmail(email).then(function() {
            ctrl.error = "Password reset email sent."
        }, function() {
            ctrl.error = "Could not send password reset email.  Please try again later.";
        });
	};

    ctrl.activate = function(username, token) {
        ctrl.error = undefined;
        var username = $location.search("username");
        var token = $location.search("token");
        if (!token || ! username) {
            ctrl.error = "Invalid credentials.";
            return;
        }

        Account.activate(username, token).then(function() {
            $location.path("#/list");
        }, function() {
            ctrl.error = "Could not activate accounts.";
        });
    };
}]);