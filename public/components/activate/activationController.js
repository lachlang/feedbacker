/*
 * Controller for password reset
 */
fbControllers.controller('ActivationCtrl',  ['$scope', '$log', '$location', 'Account', function($scope, $log, $location, Account) {

    var ctrl = this;

    ctrl.error = undefined;
    ctrl.message = undefined;

	ctrl.sendActivationEmail = function() {
		$log.info("forgotPassword");
		alert("This exciting new feature is coming soon :)");
	};

    ctrl.activate() = function() {
        ctrl.error = undefined;
        var username = $location.search("username");
        var token = $location.search("token");
        if (currentPassword != currentPasswordCheck) {
            ctrl.error = "Passwords must match.";
            return;
        } else if (!token || ! username) {
            ctrl.error = "Invalid credentials.";
            return;
        }

        Account.changePassword()


    }
}]);