package com.example.doancn





import java.util.Properties
import javax.mail.Authenticator
import javax.mail.*
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


object JavaMailSender {
    fun sendEmail(toEmail: String, subject: String, message: String): Boolean {
        val username = "lehuuminhquan0102@gmail.com";
        val password = "ogkr zeee aaii snaj"

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication? {
                return PasswordAuthentication(username, password)
            }
        })

        return try{
            val mimeMessage = MimeMessage(session)
            mimeMessage.setFrom(InternetAddress(username))
            mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(toEmail))
            mimeMessage.subject=subject
            mimeMessage.setText(message)

            Transport.send(mimeMessage)
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }

    }
}
