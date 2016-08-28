Then(/^I install the app$/) do
  reinstall_apps
end

Then(/^I start the app$/) do
  start_test_server_in_background
  backdoor('startTest')
end

Then(/^I wait for the unit test result$/) do
  wait_for_element_exists("textview {text BEGINSWITH 'TEST'}", timeout: 60*10)
end

Then(/^I print it to stdout$/) do
  s = query("edittext", :getText).first.gsub("URI: //ready\nparams: {json={}\n}", "").gsub("URI: //ping\nparams: {json={}\n}", "")
  puts s
  $stdout.puts s
  File.open("../result.log", 'w') do |file|
    file.write(s)
  end
end

Then(/^I fail if the unit test did not succeed$/) do
  if element_exists("textview text:'TEST FAILED'")
    raise "Unit test failed"
  end
end
