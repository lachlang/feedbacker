/*
 * Controller for password reset
 */
fbControllers.controller('ResetCtrl',  ['$scope', '$log', '$location', 'Account', function($scope, $log, $location, Account) {

    var ctrl = this;

    ctrl.error = undefined;
    ctrl.message = undefined;

	ctrl.sendPasswordResetEmail = function(email) {
        ctrl.error = undefined;
        ctrl.message = undefined;
        Account.sendPasswordResetEmail(email).then(function() {
            ctrl.error = "Password reset email sent."
        }, function() {
            ctrl.error = "Could not send password reset email.  Please try again later.";
        });
	};

    ctrl.resetPassword = function(newPassword, passwordCheck) {
        ctrl.error = undefined;
        ctrl.message = undefined;
        var username = $location.search("username");
        var token = $location.search("token");
        if (newPassword != passwordCheck) {
            ctrl.error = "Passwords must match.";
            return;
        } else if (!token || ! username) {
            ctrl.error = "Invalid credentials. Unable to reset your password.";
            return;
        } else if (!ctrl.newPassword || ctrl.newPassword.length < 8) {
            ctrl.error = "Please choose a password of minimum 8 characters in length."
        }

        Account.resetPassword(ctrl.newPassword, token, username).then(function() {
            $location.search("username", null);
            $location.search("token", null);
            $location.path('#/list');
        }, function() {
            ctrl.error = "Could not reset your password.  Please try again later."
        });
    }
}]);