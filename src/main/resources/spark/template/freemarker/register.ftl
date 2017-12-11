<#import "masterTemplate.ftl" as layout />

<@layout.masterTemplate title="Sign In">
<h2>Register</h2>
<p>${message}</p>
<form action="/autorenew/register" method="post">
    <dl>
        <dt>Apply number:
        <dd><input type="text" name="username" size="30" maxlength="50" placeholder="Apply Number" >
        <dt>Password:
        <dd><input type="password" name="password" size="30" placeholder="Password">
    </dl>
    <div class="actions"><input type="submit" value="Join the laziness"></div>
    <h3>Please note!</h3>
    <ul>
        <li>I store your apply-number and pass to auto-apply for you every week</li>
        <li><b>I don't encrypt passwords!</b></li>
        <li>I use Prepared-statements to avoid sql-injection</li>
        <li>The website <b>doesn't</b> encrypt your traffic. Make sure you are on a secure internet!</li>
    </ul>
    <p>And most importantly. This is a "for fun" project. I can't guarantee the server will stay up.<br>
    So if you are willing to take the "risks", you can sign up.<br>
    If you have any questions, feel free to email me at my private email: mortengerdes7000@gmail.com</p>
</form>
</@layout.masterTemplate>