/*
 * Controller for feedback action
 */
fbControllers.controller('ProfileCtrl',  ['$log', 'Model', function($log, Model) {

	var ctrl = this;
	ctrl.error = undefined;
	ctrl.message = undefined;

    ctrl.initialise = function() {
        Model.getCurrentUser().then(function(result) {
            ctrl.name = result.name
            ctrl.role = result.role
            ctrl.managerEmail = result.managerEmail
        });
    }

	ctrl.update = function() {
		ctrl.message = undefined;
		ctrl.error = undefined;

		if (!ctrl.name || !ctrl.role) {
		    ctrl.error = "Cannot set blank values."
		    return
		}

		Model.updateCurrentUser(ctrl.name, ctrl.role).then(function(response) {
			ctrl.initialise();
			ctrl.message = "Your profile details have been successfully updated."
		}, function(response) {
			ctrl.error = "We could not update your profile at this time."
			$log.error(response)
		});
	};

    ctrl.initialise();

}]);