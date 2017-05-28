fbServices.service('Util', function() {
  return {

    isInteger: function(value) {
       return typeof value === 'number' &&
         isFinite(value) &&
         Math.floor(value) === value;
    },

    isValidEmail: function(word) {
  		var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	  	return re.test(word);
	  }
  }
});