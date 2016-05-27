/*
 * Controller for password reset
 */
fbControllers.controller('ResetCtrl',  ['$scope', '$log', '$location', 'Account', function($scope, $log, $location, Account) {

    var ctrl = this;

    ctrl.error = undefined;
    ctrl.message = undefined;

	ctrl.sendPasswordResetEmail = function() {
		$log.info("forgotPassword");
		alert("This exciting new feature is coming soon :)");
	};

    ctrl.changePassword = function(currentPassword, currentPasswordCheck, newPassword) {
        ctrl.error = undefined;
        var username = $location.search("username");
        var token = $location.search("token");
        if (currentPassword != currentPasswordCheck) {
            ctrl.error = "Passwords must match.";
            return;
        }

        Account.changePassword(currentPassword, newPassword).then(function() {
            ctrl.message = "Password updated."
        }, function(){
            ctrl.error = "Could not change password."
        });
    }

    ctrl.resetPassword = function() {
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

//        Account.changePassword()


    }
}]);