/*
 * Controller for password reset
 */
fbControllers.controller('RegistrationCtrl',  ['$scope', '$log', '$location', 'Account', function($scope, $log, $location, Account) {

    var ctrl = this;

    ctrl.error = undefined;

	ctrl.sendPasswordResetEmail = function() {
		$log.info("forgotPassword");
		alert("This exciting new feature is coming soon :)");
	};

    ctrl.changePassword(currentPassword, currentPasswordCheck, newPassword) = function() {
        if (currentPassword != currentPasswordCheck) {
            ctrl.error = "Passwords must match."
            return;
        }
//        $location.search()
    }
}]);